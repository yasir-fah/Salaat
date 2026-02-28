package com.finalproject.tuwaiqfinal.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finalproject.tuwaiqfinal.Api.ApiException;
import com.finalproject.tuwaiqfinal.DTOout.AnalyseGameDTO;
import com.finalproject.tuwaiqfinal.DTOout.ReviewFeedbackDTO;
import com.finalproject.tuwaiqfinal.Model.Booking;
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
      return new AnalyseGameDTO(null, null, null, null, null, null);
    }
  }

  public ReviewFeedbackDTO hallFeedback(List<ReviewHall> reviews) {

    // validate from reviews list
    if (reviews.isEmpty()) {
      throw new ApiException("No reviews available for analysis");
    }

    // extract the comment only using stream()
    String allComment = reviews.stream()
        .map(ReviewHall::getComment)
        .filter(comment -> comment != null && !comment.trim().isEmpty())
        .collect(Collectors.joining("-"));

    // set up the prompt
    String prompt = """
          قم بتحليل التعليقات التالية التي كتبها العملاء عن القاعة.
        أجب فقط بصيغة JSON صحيحة بدون أي شروح أو نصوص إضافية.
        يجب أن تطابق الحقول التالية:

        {
          "summary": "ملخص عام عن التعليقات",
          "generalImpression": "إيجابي | سلبي | محايد",
          "positivePoints": ["قائمة النقاط الإيجابية"],
          "negativePoints": ["قائمة الملاحظات السلبية"],
          "recommendations": ["قائمة النصائح العملية"]
        }

        التعليقات:
        %s

        """.formatted(allComment);

    try {
      String response = chatClient.prompt(prompt).call().content();
      response = response.replaceAll("```json", "")
          .replaceAll("```", "")
          .trim();

      ObjectMapper mapper = new ObjectMapper();
      return mapper.readValue(response, ReviewFeedbackDTO.class);
    } catch (Exception e) {
      System.out.println(e.getMessage());
      throw new ApiException("Unable to analyze reviews at this time.");
    }
  }

  public String subHallFeedback(List<ReviewSubHall> reviews) {

    // validate from reviews list
    if (reviews.isEmpty()) {
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
      return chatClient
          .prompt(prompt)
          .call()
          .content();
    } catch (Exception e) {
      return "Unable to analyze reviews at this time. Please try again later.";
    }
  }

  public String userBookingFeedback(List<Booking> bookings) {
    if (bookings == null || bookings.isEmpty()) {
      throw new ApiException("no bookings for the user");
    }

    String systemPrompt = """
        أنت مستشار أعمال لقاعات المناسبات. اكتب تقريرًا موجّهًا للعميل بالعربية الفصحى، بنبرة مهنية وواضحة تركّز على ما يهم الإدارة واتخاذ القرار.
        قيود الإخراج:
        - نص عادي فقط، بدون Markdown أو نجوم أو إيموجي.
        - رتّب الناتج بعناوين مرقّمة 1), 2), 3)...
        - لغة موجزة، جُمل قصيرة مباشرة، ولا تذكر خطوات التفكير الداخلية.
        افتراضات:
        - المنطقة الزمنية: Asia/Riyadh، والتواريخ ميلادية.
        - العملة: SAR (ريال سعودي). قرّب القيم المالية إلى منزلتين عشريتين.
        - الفترة الزمنية تُستنتج من حقول startAt/endAt. استخدم created_at لحساب مهلة الحجز (Lead Time) عندما يكون ذلك مناسبًا.
        تطبيع الحقول (Booking.status):
        - confirmed, approved ⇒ "مؤكد"
        - canceled, cancelled ⇒ "ملغي"
        - initiated ⇒ "معلّق"
        ما ينبغي استخراجه من عناصر Booking (تجاهل ما لا يتوفر):
        - المعرّف id، الحالة status، القاعة/القسم subHall (الاسم أو المعرّف)، عدد الأعضاء/الضيوف members، مدة الحجز بالدقائق duration_minutes، السعر الإجمالي totalPrice، تاريخ الإنشاء created_at، وقت البداية startAt، وقت النهاية endAt، اللعبة/النشاط game (إن وُجد)، الدفعات payments (لإجمالي المدفوع).
        قواعد الحساب:
        - إجمالي الإيراد = مجموع totalPrice للحجوزات "المؤكدة" (يمكن ذكر إجماليات لكل الحالات عند الحاجة).
        - متوسط قيمة الحجز = إجمالي الإيراد ÷ عدد الحجوزات المؤكَّدة.
        - متوسط الضيوف = متوسط members حيثما توافر.
        - متوسط المدة = متوسط duration_minutes حيثما توافر.
        - التحصيل: إذا وُجدت payments احسب إجمالي المدفوع والمتبقي = مجموع totalPrice − إجمالي المدفوع، واذكر نسبة التحصيل = المدفوع ÷ مجموع totalPrice.
        - التوزيعات: حسب اليوم من الأسبوع (من startAt)، حسب القاعة/القسم (subHall)، وحسب اللعبة/النشاط (game). اذكر أعلى 3 في كل توزيع.
        - الأداء: أفضل وأسوأ 3 قاعات/أنشطة من حيث العدد والإيراد.
        - الإلغاء: احصر الحجم الزمني ونِسب الإلغاء. إذا لم تتوافر أسباب الإلغاء فاذكر عدم التوافر صراحة ضمن "الفجوات".
        - الاتجاهات: أبرز مواسم/أيام/ساعات الذروة (بالاعتماد على تكرار startAt). إن أمكن، أشر إلى تغيّرٍ ملحوظ (ارتفاع/انخفاض) عبر الزمن.
        - نظرة قادمة: تقدير تقريبي للحجوزات في 14 يومًا قادمة اعتمادًا على المتوسطات الحالية (اذكر أنه تقديري وقد لا يعكس الواقع).
        - التعارضات: نبّه إلى أي حجوزات متداخلة لنفس القاعة (overlaps) إن وُجدت.
        - الإشغال/الاستفادة: إذا كانت سعة القاعة متاحة عبر subHall.capacity احسب نسبة الاستفادة الزمنية = مجموع الدقائق المحجوزة ÷ الدقائق المتاحة خلال الفترة. إن تعذّر، اذكر السبب ضمن "الفجوات".
        معالجة البيانات الكبيرة أو غير المتناسقة:
        - إن كانت البيانات كبيرة جدًا، لخّص آخر 90 يومًا أولًا ثم ألمِح بإيجاز إلى الفترات الأقدم.
        - كن مرنًا مع تنسيقات الإدخال (JSON/نص حر/toString)، واستخرج ما أمكن من الحقول المذكورة أعلاه.
        """;

    String userPrompt = """
        أنتج تقريرًا موجّهًا للعميل بناءً على قائمة الحجوزات (نموذج Booking في جافا). هذه هي البيانات:
        %s

        المطلوب (اكتب الناتج كنص عادي مرتب ومرقّم):
        1) ملخص تنفيذي للفترة المغطّاة: تاريخ البداية والنهاية وعدد الأيام، مع جملة توضح الصورة العامة (هدوء/نشاط/موسمية).
        2) مؤشرات أساسية: إجمالي الحجوزات، "المؤكد"، "الملغي"، "المعلّق"، إجمالي الإيراد (SAR) للحجوزات المؤكدة، متوسط قيمة الحجز، متوسط عدد الأعضاء (members)، ومتوسط المدة (بالدقائق). إن توفّر payments فاذكر إجمالي المدفوع، المتبقي، ونسبة التحصيل.
        3) التوزيعات: أعلى 3 أيام في الأسبوع، أعلى 3 قاعات/أقسام (subHall)، وأعلى 3 ألعاب/أنشطة (game) من حيث عدد الحجوزات والإيراد.
        4) الأداء: أفضل 3 وأسوأ 3 قاعات/أنشطة من حيث العدد والإيراد (إن أمكن).
        5) الإلغاء: حجم الإلغاء ونسبته عبر الزمن. إذا توفّرت أي إشارات عن توقيت الإلغاء (بالقرب من startAt أو بعد الحجز بمدة قصيرة) فاذكرها. إن لم تتوفر أسباب الإلغاء فاذكر عدم توافرها صراحة.
        6) الاتجاهات وأوقات الذروة: الأيام/الساعات الأكثر طلبًا، وأي ارتفاع/انخفاض ملموس. احسب مهلة الحجز (Lead Time) التقريبية = متوسط (startAt − created_at) إن توافرت الحقول.
        7) النظرة القادمة: تقدير تقريبي لعدد الحجوزات في الأيام الـ 14 المقبلة اعتمادًا على متوسطات وتوزيعات الفترة الحالية، واذكر أنه تقدير تقريبي وقد يتغيّر.
        8) الفجوات والقيود: ما تعذّر حسابه ولماذا (مثل غياب capacity أو أسباب الإلغاء أو نقص startAt/endAt أو تفاصيل payments).
        9) توصيات عملية قصيرة قابلة للتنفيذ: تسعير، عروض لفترات الذروة/الهدوء، تحسين الجدولة وتوزيع القاعات، سياسات دفع/إلغاء، وتنبيهات لتجنّب التعارضات.

        ملاحظات:
        - إذا كانت البيانات غير كافية للتنبؤ أو لاحتساب مؤشر معيّن، اذكر ذلك صراحة ضمن "الفجوات".
        - إن وُجدت حجوزات متداخلة لنفس القاعة في نفس الفترة، نبّه إليها ضمن "التوصيات".
        """
        .formatted(bookings.toString());

    return chatClient
        .prompt()
        .options(OpenAiChatOptions.builder()
            .model("gpt-4o")
            .temperature(0.2)
            .build())
        .system(systemPrompt)
        .user(userPrompt)
        .call()
        .content();
  }

}
