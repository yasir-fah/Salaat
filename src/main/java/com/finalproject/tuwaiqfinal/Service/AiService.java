package com.finalproject.tuwaiqfinal.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproject.tuwaiqfinal.Api.ApiException;
import com.finalproject.tuwaiqfinal.DTOout.AnalyseGameDTO;
import com.finalproject.tuwaiqfinal.Model.ReviewHall;
import com.finalproject.tuwaiqfinal.Model.ReviewSubHall;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AiService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public AnalyseGameDTO analyzeImage(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new ApiException("image bytes are empty");
        }


        String systemPrompt = """
                أنت مساعد بصري متخصص في تحليل صورة واحدة فقط.
                أجب بالعربية فقط.
                أعد مخرجاتك بصيغة JSON فقط دون أي نص إضافي.
                """;

        String userPrompt = """
                حلّل الصورة وأعد JSON صالح فقط:
                
                {
                  "اسم_اللعبة": "<أقرب اسم لعبة يمكن تحديده>",
                  "عدد_اللاعبين": "<العدد المعتاد أو نطاق اللاعبين>",
                  "كيفية_اللعب": "<شرح تفصيلي: الهدف، الخطوات الرئيسية، القواعد الأساسية>",
                  "بدائل_ممكنة": ["لعبة بديلة 1", "لعبة بديلة 2"],
                  "مستوى_الصعوبة": "<سهل / متوسط / صعب>",
                  "نصائح_للعبة": ["نصيحة 1", "نصيحة 2", "نصيحة 3"]
                }
                
                قواعد:
                - لا تُرجع أي نص خارج JSON.
                - إذا كانت الأدوات شائعة وتُستخدم في أكثر من لعبة (مثل أوراق كوتشينة أو نرد)، اذكر اسم اللعبة الأكثر ترجيحًا وضع البدائل في "بدائل_ممكنة".
                - لا تترك أي حقل فارغ. إذا غير معروف، ضع "غير محدد".
                - "مستوى_الصعوبة" يُقيَّم بشكل تقريبي بناءً على شيوع القواعد وسهولة تعلمها.
                - "نصائح_للعبة" يجب أن تكون عملية ومباشرة (مثل: التركيز على قطع الخصم، إدارة الأوراق، أو التخطيط للحركات القادمة).
                """;


        ByteArrayResource resource = new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return "upload.png";
            }
        };

        log.info("start analyzing game");
        String content = chatClient
                .prompt()
                .options(OpenAiChatOptions.builder()
                        .model("gpt-4o")
                        .temperature(0.2)
                        .build())
                .system(systemPrompt)
                .user(u -> u.text(userPrompt).media(MediaType.IMAGE_PNG, resource))
                .call()
                .content();
        log.info("finished analyzing game");


        if (content.startsWith("```")) {
            int first = content.indexOf('{');
            int last = content.lastIndexOf('}');
            if (first >= 0 && last > first) {
                content = content.substring(first, last + 1);
            }
        }


        try {
            return objectMapper.readValue(content, AnalyseGameDTO.class);
        } catch (Exception e) {
            log.error("Failed to analyze image / parse JSON", e);
            return new AnalyseGameDTO(null,null,null,null,null,null);
        }
    }

    public String hallFeedback(List<ReviewHall> reviews) {

        // validate from reviews list
        if(reviews.isEmpty()) {
            throw new ApiException("No reviews available for analysis");
        }

        // extract the comment only using stream()
        String allComment = reviews.stream()
                .map(ReviewHall::getComment)
                .filter(comment -> comment != null && !comment.trim().isEmpty())
                .collect(Collectors.joining("-"));

        // set up the prompt
        String prompt = """
                قم بتحليل التعليقات التالية التي كتبها العملاء عن القاعة:
                الجواب يكون نص عادي بدون أي علامات Markdown أو نجوم
                %s
                
                المطلوب منك:
                1. إعطاء ملخص عام عن هذه التعليقات.
                2. تحديد الانطباع العام (إيجابي / سلبي / محايد).
                3. ذكر أبرز النقاط الإيجابية التي يكررها العملاء.
                4. استخراج التعليقات السلبية أو الملاحظات المتكررة.
                5. تقديم نصائح عملية موجهة لصاحب القاعة بناءً على هذه الملاحظات السلبية (كيف يمكنه تحسين القاعة أو الخدمة).
                
                
                """.formatted(allComment);

        try {
            log.info("start feedback analyzing");
            return chatClient
                    .prompt(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error(e.getMessage());
            return e.getMessage();
        }
    }

    public String subHallFeedback(List<ReviewSubHall> reviews) {

        // validate from reviews list
        if(reviews.isEmpty()) {
            throw new ApiException("No reviews available for analysis");
        }

        // extract the comment only using stream()
        String allComment = reviews.stream()
                .map(ReviewSubHall::getComment)
                .filter(comment -> comment != null && !comment.trim().isEmpty())
                .collect(Collectors.joining("-"));

        // set up the prompt
        String prompt = """
                قم بتحليل التعليقات التالية التي كتبها العملاء عن القاعه الداخليه اللتي تتضمن نوع من انوع الألعاب:
                الجواب يكون نص عادي بدون أي علامات Markdown أو نجوم
                ايضا يكون الجواب مرتب ومرقم
                %s
                
                المطلوب منك:
                1. إعطاء ملخص عام عن هذه التعليقات.
                2. تحديد الانطباع العام (إيجابي / سلبي / محايد).
                3. ذكر أبرز النقاط الإيجابية التي يكررها العملاء.
                4. استخراج التعليقات السلبية أو الملاحظات المتكررة.
                5. تقديم نصائح عملية موجهة لصاحب القاعة بناءً على هذه الملاحظات السلبية (كيف يمكنه تحسين اللعبه أو الخدم .
                
                
                """.formatted(allComment);

        try {
            log.info("start analyzing subHall feedbacks");
            return chatClient
                    .prompt(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            return "Unable to analyze reviews at this time. Please try again later.";
        }
    }
}
