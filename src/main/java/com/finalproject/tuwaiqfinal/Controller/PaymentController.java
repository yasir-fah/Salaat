package com.finalproject.tuwaiqfinal.Controller;

import com.finalproject.tuwaiqfinal.Api.ApiException;
import com.finalproject.tuwaiqfinal.DTOin.PaymentRequest;
import com.finalproject.tuwaiqfinal.DTOout.MoyasarPaymentResponseDTO;
import com.finalproject.tuwaiqfinal.DTOout.PaymentCreationResponseDTO;
import com.finalproject.tuwaiqfinal.Model.Game;
import com.finalproject.tuwaiqfinal.Service.GameService;
import com.finalproject.tuwaiqfinal.Service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/payments")
@RequiredArgsConstructor
public class    PaymentController {
    private final PaymentService paymentService;
    private final GameService gameService;

    // post new general payment
    @PostMapping("/card")
    public MoyasarPaymentResponseDTO processPayment(@RequestBody PaymentRequest paymentRequest){
        return paymentService.processPayment(paymentRequest);
    }

    // get transaction detail:
    @GetMapping("/get/status/{statusId}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable String statusId){
        return ResponseEntity.status(200).body(paymentService.getPaymentStatus(statusId));
    }

    // payment by user
    @PostMapping("/pay/by/{customerId}/for/{bookingId}")
    public ResponseEntity<PaymentCreationResponseDTO> customerPayment(@PathVariable Integer customerId,
                                                                      @PathVariable Integer bookingId,
                                                                      @RequestBody PaymentRequest paymentRequest) {
        PaymentCreationResponseDTO result =  paymentService.customerPayment(customerId,bookingId,paymentRequest);
        return ResponseEntity.status(200).body(result);
    }


    // Callback endpoint to handle Moyasar response
    @GetMapping("/callback")
    public ResponseEntity<String> handlePaymentCallback(
            @RequestParam("id") String paymentId,
            @RequestParam("status") String status,
            @RequestParam("message") String message) {

        try {
            // Update payment and booking status
            paymentService.handlePaymentCallback(paymentId, status, message);

            // Return success response to Moyasar
            return ResponseEntity.ok("Payment callback processed successfully");

        } catch (Exception e) {
            // Log the error and return failure response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process payment callback: " + e.getMessage());
        }
    }

    @GetMapping(value = "/download/invoice/{bookingId}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadFile(@PathVariable("bookingId") Integer bookingId) {
        byte[] data = paymentService.downloadInvoice(bookingId);
        if (data == null || data.length == 0) {
            throw new ApiException("Invoice not found or empty");
        }


        String filename = "invoice-" + bookingId + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(data.length)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + java.net.URLEncoder.encode(filename, java.nio.charset.StandardCharsets.UTF_8))
                .body(data);
    }

}



