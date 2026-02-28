package com.finalproject.tuwaiqfinal.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproject.tuwaiqfinal.Api.ApiException;
import com.finalproject.tuwaiqfinal.DTOin.PaymentRequest;
import com.finalproject.tuwaiqfinal.DTOout.MoyasarPaymentResponseDTO;
import com.finalproject.tuwaiqfinal.DTOout.PaymentCreationResponseDTO;
import com.finalproject.tuwaiqfinal.DTOout.PaymentDTO;
import com.finalproject.tuwaiqfinal.Model.Booking;
import com.finalproject.tuwaiqfinal.Model.Customer;
import com.finalproject.tuwaiqfinal.Model.Game;
import com.finalproject.tuwaiqfinal.Model.Payment;
import com.finalproject.tuwaiqfinal.Repository.BookingRepository;
import com.finalproject.tuwaiqfinal.Repository.CustomerRepository;
import com.finalproject.tuwaiqfinal.Repository.GameRepository;
import com.finalproject.tuwaiqfinal.Repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${moyaser.api.key}")
    private String apiKey;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String MOYASAR_API_URL = "https://api.moyasar.com/v1/payments/";

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private BookingRepository bookingRepository;

    private static final String CURRENCY = "SAR";
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private GameRepository gameRepository;


    private final InvoicePdfService invoicePdfService;
    private final S3Service s3Service;
    private final MailService mailService;

    // create method for Payment process:
    public MoyasarPaymentResponseDTO processPayment(PaymentRequest paymentRequest) {


        String url = "https://api.moyasar.com/v1/payments";

        // callback url, transaction information will be sent to this url:
        String callbackUrl = "http://localhost:8080/api/payments/callback";

        // creating request body
        String requestBody = String.format(
                "source[type]=card&source[name]=%s&source[number]=%s&source[cvc]=%s" +
                        "&source[month]=%s&source[year]=%s&amount=%d&currency=%s" +
                        "&callback_url=%s",
                paymentRequest.getName(),
                paymentRequest.getNumber(),
                paymentRequest.getCvc(),
                paymentRequest.getMonth(),
                paymentRequest.getYear(),
                (int) (paymentRequest.getAmount() * 100), // Convert to smallest currency unit
                paymentRequest.getCurrency(),
                callbackUrl
        );


        // Build headers
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(apiKey, ""); // API key as username, blank password
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Wrap body + headers
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        // Send the POST request to callback url:
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<MoyasarPaymentResponseDTO> response = restTemplate.exchange(url,
                HttpMethod.POST, entity, MoyasarPaymentResponseDTO.class);

        return response.getBody();
    }


    // 1- get payment status
    public String getPaymentStatus(Integer customerId,String paymentId){

        // check if customer exist
        Customer customer = customerRepository.findCustomerById(customerId);
        if(customer == null) {
            throw new ApiException("customer not found");
        }

        // check if customer belong to this payment
        Payment payment = paymentRepository.getPaymentByMoyasarPaymentId(paymentId);
        if(payment == null) {
            throw new ApiException("payment not found");
        }

        if(!payment.getCustomer().getId().equals(customer.getId())) {
            throw new ApiException("payment does not belong to this user");
        }

        // Prepare headers with authentication
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(apiKey, ""); // Basic Auth (API Key as username, blank password)
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create HTTP request entity (headers only)
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Call Moyasar API
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                MOYASAR_API_URL + paymentId,
                HttpMethod.GET,
                entity,
                String.class
        );

        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(response.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String result = jsonNode.get("status").asText();
        return "status of payment is "+ result + " with id of:" + jsonNode.get("id").asText().substring(0,8);
    }


    // 2- create a payment by customer
    public PaymentCreationResponseDTO customerPayment(Integer customerId,
                                                      Integer bookingId,
                                                      PaymentRequest paymentRequest) {


        /// 1- check if customer exist
        Customer customer = customerRepository.findCustomerById(customerId);
        if(customer == null) {
            throw new ApiException("customer not found");
        }
        /// 2- check from booking exist
        Booking booking = bookingRepository.findBookingsById(bookingId);
        if(booking == null) {
            throw new ApiException("booking not found");
        }
        /// 3- check if booking belong to user
        if(booking.getCustomer() == null || !booking.getCustomer().getId().equals(customer.getId())){
            throw new ApiException("customer does not belong to this booking");
        }
        /// 4- check from booking status
        if(!booking.getStatus().equals("initiated")) {
            throw new ApiException("booking status has been handled before");
        }

        // 4.5- Final conflict check before payment
        List<Booking> conflicts = bookingRepository.findConflictBookingsForGame(
                booking.getGame().getId(), booking.getStartAt(), booking.getEndAt());

        if(!conflicts.isEmpty()) {
            throw new ApiException("This time slot was just booked by someone else! Please try a different time");
        }



        /// 5- set payment details
        paymentRequest.setAmount(booking.getTotalPrice());
        paymentRequest.setDescription("customer booking for"+booking.getSubHall().getDescription());

        /// 6- call moyasar api for payment
        MoyasarPaymentResponseDTO moyasarResponse = processPayment(paymentRequest);


        /// 7- create payment
        Payment payment = new Payment();

        payment.setCustomer(customer);
        payment.setBooking(booking);
        payment.setAmount(paymentRequest.getAmount());
        payment.setCurrency(CURRENCY);
        payment.setStatus(moyasarResponse.getStatus());
        payment.setMoyasarPaymentId(moyasarResponse.getId());
        paymentRepository.save(payment);

        /// save url
        String url = resolveTransactionUrl(moyasarResponse);
        return new PaymentCreationResponseDTO(payment,url,payment.getStatus(), moyasarResponse.getId(), "payment has been initiated");
    }


    // 3- update status of booking depend on payment
    public void handlePaymentCallback(String moyasarPaymentId, String status, String message) {

        // 1. Find payment
        Payment payment = paymentRepository.findByMoyasarPaymentId(moyasarPaymentId);
        if (payment == null) {
            throw new ApiException("Payment not found with Moyasar ID: " + moyasarPaymentId);
        }

        payment.setPaid_at(LocalDateTime.now());

        // 2. Update payment status
        payment.setStatus(status.toLowerCase());
        paymentRepository.save(payment);

//        String newStatus = payment.getStatus();

        // 3. Update booking status directly
        if (payment.getBooking() != null) {
            Booking booking = payment.getBooking();
            if(!message.equalsIgnoreCase("approved")){
                booking.setStatus("cancelled"); // if not approved then cancelled

                // release game from booking
                bookingRepository.delete(booking);
                bookingRepository.save(booking);
            }else{
                booking.setStatus(message); // APPROVED
                bookingRepository.save(booking);

                Game game = booking.getGame();




                byte[] pdf = invoicePdfService.generateInvoicePdf(booking.getSubHall().getHall(),
                        booking.getSubHall().getHall().getOwner(),
                        booking,
                        booking.getCustomer().getUser(),
                        booking.getCustomer(),
                        booking.getSubHall(),
                        payment,
                        game);

                //save invoice in s3
                s3Service.uploadBytes(payment.getMoyasarPaymentId(), pdf, "application/pdf");
                mailService.sendInvoiceWithAttachment(
                        booking.getCustomer().getUser().getEmail(),
                        booking.getCustomer().getUser().getUsername(),
                        payment.getId().toString(),
                        payment.getPaid_at(),
                        booking.getTotalPrice(),
                        payment.getCurrency(),
                        "http://final-server-env.eba-fitwn3ht.eu-central-1.elasticbeanstalk.com/",
                        pdf,
                        "invoice-"+payment.getMoyasarPaymentId()+".pdf"

                        );



            }
        }
    }

    // Helper method to resolve transaction URL from Moyasar response
    private String resolveTransactionUrl(MoyasarPaymentResponseDTO moyasarResponse) {
        String transactionUrl = moyasarResponse.getTransaction_url();
        if (transactionUrl == null && moyasarResponse.getSource() != null) {
            transactionUrl = moyasarResponse.getSource().getTransaction_url();
        }
        return transactionUrl;
    }
    // -4
    public byte[] downloadInvoice(Integer bookingId){
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(()->new ApiException("booking not found"));

        String moyasarPaymentId = paymentRepository.findByBooking(booking)
                .getMoyasarPaymentId();
        return s3Service.downloadBytes(moyasarPaymentId);
    }


    // 5- get all payment by one customer:
    public List<PaymentDTO> getAllPaymentByCustomer(Integer customerId) {

        // check if customer exist
        Customer customer = customerRepository.findCustomerById(customerId);
        if(customer == null) {
            throw new ApiException("customer not found");
        }

        //return payments
        List<Payment> payments = paymentRepository.findAllByCustomerId(customerId);

        List<PaymentDTO> paymentDTOs = payments.stream()
                .map(payment -> {
                    PaymentDTO dto = new PaymentDTO();
                    dto.setId(payment.getId());
                    dto.setAmount(payment.getAmount());
                    dto.setCurrency(payment.getCurrency());
                    dto.setStatus(payment.getStatus());
                    dto.setPaid_at(payment.getPaid_at());
                    dto.setCreated_at(payment.getCreated_at());
                    return dto;
                })
                .collect(Collectors.toList());

        return paymentDTOs;
    }


    // 6- get payment by specific status
    public List<PaymentDTO> getAllPaymentByCustomerAndStatus(Integer customerId, String status) {

        // check if customer exist
        Customer customer = customerRepository.findCustomerById(customerId);
        if (customer == null) {
            throw new ApiException("customer not found");
        }

        // get all payments by customer and status
        List<Payment> payments = paymentRepository.findAllByCustomerIdAndStatus(customerId, status);

        // map to DTO
        return payments.stream()
                .map(payment -> {
                    PaymentDTO dto = new PaymentDTO();
                    dto.setId(payment.getId());
                    dto.setAmount(payment.getAmount());
                    dto.setCurrency(payment.getCurrency());
                    dto.setStatus(payment.getStatus());
                    dto.setPaid_at(payment.getPaid_at());
                    dto.setCreated_at(payment.getCreated_at());
                    return dto;
                })
                .collect(Collectors.toList());
    }



}
