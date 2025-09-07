package com.finalproject.tuwaiqfinal.Service;

import com.adobe.pdfservices.operation.PDFServices;
import com.adobe.pdfservices.operation.PDFServicesMediaType;
import com.adobe.pdfservices.operation.PDFServicesResponse;
import com.adobe.pdfservices.operation.io.Asset;
import com.adobe.pdfservices.operation.io.StreamAsset;
import com.adobe.pdfservices.operation.pdfjobs.jobs.DocumentMergeJob;
import com.adobe.pdfservices.operation.pdfjobs.params.documentmerge.DocumentMergeParams;
import com.adobe.pdfservices.operation.pdfjobs.params.documentmerge.OutputFormat;
import com.adobe.pdfservices.operation.pdfjobs.result.DocumentMergeResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.finalproject.tuwaiqfinal.Model.*;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Map;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class InvoicePdfService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvoicePdfService.class);

    private static final ZoneId KSA = ZoneId.of("Asia/Riyadh");
    private static final DateTimeFormatter HUMAN_TS =
            DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm a").withLocale(Locale.ENGLISH);

    private static String fmt(LocalDateTime ldt) {
        return (ldt == null) ? "" : ldt.atZone(KSA).format(HUMAN_TS);
    }




    private final PDFServices pdfServices;


    @Value("${adobe.pdfservices.templatePath}")
    private Resource templateDocx;


    public byte[] generateInvoicePdf(Hall hall, Owner owner, Booking booking, User user, Customer customer,SubHall subHall, Payment payment, Game game) {
        LOGGER.info("Generating PDF for booking ID: {}", booking.getId());

        if (hall == null) {
            LOGGER.error("Hall is null for booking ID: {}", booking.getId());
            throw new IllegalArgumentException("Hall cannot be null");
        }
        if (owner == null) {
            LOGGER.error("Owner is null for booking ID: {}", booking.getId());
            throw new IllegalArgumentException("Owner cannot be null");
        }
        if (booking == null) {
            LOGGER.error("Booking is null");
            throw new IllegalArgumentException("Booking cannot be null");
        }
        if (user == null) {
            LOGGER.error("User is null for booking ID: {}", booking.getId());
            throw new IllegalArgumentException("User cannot be null");
        }
        if (customer == null) {
            LOGGER.error("Customer is null for booking ID: {}", booking.getId());
            throw new IllegalArgumentException("Customer cannot be null");
        }
        if (subHall == null) {
            LOGGER.error("SubHall is null for booking ID: {}", booking.getId());
            throw new IllegalArgumentException("SubHall cannot be null");
        }
        if (payment == null) {
            LOGGER.error("Payment is null for booking ID: {}", booking.getId());
            throw new IllegalArgumentException("Payment cannot be null");
        }
        if (game == null) {
            LOGGER.error("Game is null for booking ID: {}", booking.getId());
            throw new IllegalArgumentException("Game cannot be null");
        }

        try (InputStream templateStream = templateDocx.getInputStream()) {


            Asset templateAsset = pdfServices.upload(
                    templateStream,
                    PDFServicesMediaType.DOCX.getMediaType()
            );

            // 2) Build the JSON payload: { "booking": { ... }, "invoice": { ... } }
            ObjectMapper mapper = new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            Map<String, Object> hallMap =
                    mapper.convertValue(hall, new TypeReference<>() {});

            Map<String, Object> ownerMap =
                    mapper.convertValue(owner, new TypeReference<>() {});

            Map<String, Object> bookingMap =
                    mapper.convertValue(booking, new TypeReference<>() {});

            Map<String, Object> userMap =
                    mapper.convertValue(user, new TypeReference<>() {});

            Map<String, Object> customerMap =
                    mapper.convertValue(customer, new TypeReference<>() {});

            Map<String, Object> subHallMap =
                    mapper.convertValue(subHall, new TypeReference<>() {});

            Map<String, Object> paymentMap =
                    mapper.convertValue(payment, new TypeReference<>() {});

            Map<String, Object> gameMap =
                    mapper.convertValue(game, new TypeReference<>() {});

            bookingMap.put("startAt",    fmt(booking.getStartAt()));
            bookingMap.put("endAt",      fmt(booking.getEndAt()));
            bookingMap.put("created_at", fmt(booking.getCreated_at()));
            paymentMap.put("paid_at",    fmt(payment.getPaid_at()));


            JSONObject data = new JSONObject()
                    .put("hall", new JSONObject(hallMap))
                    .put("owner", new JSONObject(ownerMap))
                    .put("booking", new JSONObject(bookingMap))
                    .put("user", new JSONObject(userMap))
                    .put("customer", new JSONObject(customerMap))
                    .put("subHall", new JSONObject(subHallMap))
                    .put("payment", new JSONObject(paymentMap))
                    .put("game", new JSONObject(gameMap));

            // 3) Build job params: output PDF (could be DOCX if you prefer)
            DocumentMergeParams params = DocumentMergeParams
                    .documentMergeParamsBuilder()
                    .withJsonDataForMerge(data)
                    .withOutputFormat(OutputFormat.PDF)
                    .build();

            // 4) Submit merge job and fetch result
            DocumentMergeJob job = new DocumentMergeJob(templateAsset, params);
            String location = pdfServices.submit(job);

            PDFServicesResponse<DocumentMergeResult> response =
                    pdfServices.getJobResult(location, DocumentMergeResult.class);

            StreamAsset result = pdfServices.getContent(response.getResult().getAsset());
            return result.getInputStream().readAllBytes();

        } catch (Exception e) {
            LOGGER.error("Failed to generate invoice PDF", e);
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }
}
