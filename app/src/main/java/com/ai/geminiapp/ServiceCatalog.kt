package com.ai.geminiapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

object ServiceCatalog {
    // 💻 قسم كتابة وتطوير وتصحيح الأكواد البرمجية وأدواته
    val codeTools = listOf(
        ServiceToolItem(
            id = "code_generate",
            title = "توليد وكتابة الأكواد الذكية",
            subtitle = "كتابة دوال وخوارزميات وتطبيقات كاملة بأكثر من 20 لغة برمجة",
            category = "code",
            iconEmoji = "💻",
            defaultPrompt = "اكتب لي كود برمجي احترافي وموثق وشامل للمهمة التالية:",
            inputHint = "مثال: دالة Kotlin للتحقق من البريد أو سكريبت بايثون لمعالجة البيانات...",
            sampleInputs = listOf(
                "دالة Kotlin للتحقق من صحة البريد الإلكتروني وكلمة المرور",
                "سكريبت Python لقراءة وتحليل ملفات CSV واستخراج الإحصائيات",
                "واجهة كاملة في Jetpack Compose لشاشة تسجيل الدخول العصرية",
                "دالة JavaScript لحساب الفرق الزمني بين تاريخين بصيغة نسبية"
            )
        ),
        ServiceToolItem(
            id = "code_debug",
            title = "فحص وتصحيح أخطاء الأكواد (Debug)",
            subtitle = "اكتشاف الثغرات وتصحيح المشاكل البرمجية مع شرح سبب الخطأ",
            category = "code",
            iconEmoji = "🛠️",
            defaultPrompt = "قم بفحص هذا الكود واكتشاف الأخطاء البرمجية وإصلاحها مع شرح تفصيلي للسبب:",
            inputHint = "الصق الكود البرمجي أو رسالة الخطأ هنا لتصحيحها فوراً...",
            sampleInputs = listOf(
                "تصحيح خطأ NullPointerException في كود Android Kotlin",
                "فحص ثغرة SQL Injection في استعلام قاعدة بيانات وتأمينه",
                "تصحيح خطأ Async / Await مسبب لتعليق التطبيق",
                "حل مشكلة OutOfMemory في معالجة مصفوفة ضخمة"
            )
        ),
        ServiceToolItem(
            id = "code_explain",
            title = "شرح وتبسيط الأكواد المعقدة",
            subtitle = "تفكيك المنطق البرمجي وشرح الكود سطر بسطر وتوضيح التعقيد الحسابي",
            category = "code",
            iconEmoji = "🧠",
            defaultPrompt = "اشرح لي هذا الكود البرمجي بالتفصيل سطر بسطر مع توضيح منطقه الحسابي والتعقيد Time & Space Complexity:",
            inputHint = "الصق الكود الذي تريد شرحه وتوضيح منطقه...",
            sampleInputs = listOf(
                "شرح خوارزمية البحث الثنائي Binary Search خطوة بخطوة",
                "شرح نمط تصميم MVVM مع Coroutines في أندرويد",
                "شرح آلية عمل الـ Recomposition في Jetpack Compose",
                "شرح مفاهيم الـ Pointers والذاكرة في C++"
            )
        ),
        ServiceToolItem(
            id = "code_convert",
            title = "تحويل الأكواد بين اللغات البرمجية",
            subtitle = "نقل وترجمة الأكواد من أي لغة برمجية إلى لغة أخرى مع مراعاة الاصطلاحات القياسية",
            category = "code",
            iconEmoji = "🔄",
            defaultPrompt = "قم بتحويل هذا الكود البرمجي بدقة إلى اللغة المحددة مع استخدام أفضل الممارسات القياسية:",
            inputHint = "اكتب الكود واللغة الهدف (مثال: حول هذا الكود من Python إلى Kotlin)...",
            sampleInputs = listOf(
                "تحويل كود معالجة نصوص من Python إلى Kotlin",
                "تحويل كلاس Java قديم إلى Kotlin Data Class حديث",
                "تحويل كود JavaScript إلى TypeScript مع Type Definitions",
                "تحويل استعلام SQL عادي إلى Room Dao Query"
            )
        ),
        ServiceToolItem(
            id = "code_database",
            title = "هندسة قواعد البيانات و SQL",
            subtitle = "تصميم الجداول، بناء استعلامات معقدة، وإنشاء فهارس تسريع الأداء",
            category = "code",
            iconEmoji = "🗄️",
            defaultPrompt = "صمم لي هيكل قاعدة بيانات واستعلامات SQL محسنة وعالية الأداء للمتطلبات التالية:",
            inputHint = "اكتب متطلبات النظام أو الاستعلامات المطلوب بناؤها...",
            sampleInputs = listOf(
                "تصميم قاعدة بيانات لمتجر إلكتروني مع جداول المنتجات والطلبات والمستخدمين",
                "استعلام SQL مركب يجمع البيانات باستخدام JOIN و Aggregations",
                "تصميم فهارس Indexes لتسريع استعلامات البحث في جدول ضخم",
                "هيكل جداول Room مع العلاقات 1-to-Many لمستخدم وسجلاته"
            )
        )
    )

    // 🎨 قسم تصميم وتوليد وفحص الصور وأدواتها
    val imageTools = listOf(
        ServiceToolItem(
            id = "image_prompt",
            title = "توليد وتخيل الصور الفنية بالذكاء الاصطناعي",
            subtitle = "صياغة وتوليد برومبتات واقعية وتصاميم سينمائية فائقة الجودة",
            category = "image",
            iconEmoji = "🎨",
            defaultPrompt = "صمم لي وصفاً بصرياً فائق الدقة (AI Image Prompt) للمشهد التالي مع ذكر تفاصيل الإضاءة والعدسة وزاوية التصوير والأسلوب:",
            inputHint = "صف المشهد أو الفكرة التي تريد تحويلها إلى صورة إبداعية...",
            sampleInputs = listOf(
                "مدينة مستقبلية بنمط عربي أصيل تجمع بين العمارة التراثية وإضاءات النيون المتطورة",
                "لوحة زيتية كلاسيكية لطبيعة جبلية ساحرة وقت شروق الشمس وضباب خفيف",
                "تصميم ثلاثي الأبعاد 3D Isometric لمكتب مطور برمجيات مليء بالشاشات والنباتات",
                "بورتريه سينمائي احترافي بعدسة 85mm مع بوكيه ناعم وإضاءة ذهبية"
            )
        ),
        ServiceToolItem(
            id = "image_vision",
            title = "فحص وتحليل الصور البصرية",
            subtitle = "رفع أي صورة لفحص تفاصيلها الدقيقة وقراءة محتواها وشرحها",
            category = "image",
            iconEmoji = "👁️",
            defaultPrompt = "أريد فحص وتحليل الصورة المرفقة والإجابة عن هذا الاستفسار بدقة تفصيلية:",
            inputHint = "اطرح سؤالك أو المطلوب تحليله من الصورة المرفقة...",
            sampleInputs = listOf(
                "ما هي العناصر الرئيسية والرموز المعبر عنها في هذه الصورة؟",
                "اشرح لي هذا المخطط الهندسي أو الرسم البياني المرفق",
                "حل المسألة العلمية المكتوبة بخط اليد في هذه الصورة",
                "حلل التكوين الفني وتناسق الألوان في هذا التصميم"
            )
        ),
        ServiceToolItem(
            id = "image_logo",
            title = "تصميم الشعارات والهوية البصرية",
            subtitle = "ابتكار أفكار شعارات عصرية وأيقونات تطبيقات وهوية متكاملة",
            category = "image",
            iconEmoji = "✨",
            defaultPrompt = "اقترح لي أفكار شعارات وهوية بصرية عصرية ومتكاملة للمشروع التالي مع برومبتات توليدها بالذكاء الاصطناعي:",
            inputHint = "اكتب اسم المشروع، نوع نشاطه، وقيمه الأساسية...",
            sampleInputs = listOf(
                "شعار لمنصة تعليمية ذكية للأطفال تركز على الابتكار والمرح",
                "أيقونة تطبيق لياقة بدنية بتصميم تبسيطي Minimalist ولمسات ذهبية",
                "شعار لمقهى عصري فاخر يجمع بين أصالة القهوة والهدوء الحديث",
                "هوية بصرية كاملة لتطبيق ذكاء اصطناعي مساعد للمبرمجين"
            )
        ),
        ServiceToolItem(
            id = "image_ocr",
            title = "استخراج وتفريغ النصوص (OCR)",
            subtitle = "قراءة واستخراج النصوص العربية والإنجليزية من لقطات الشاشة والمستندات بدقة",
            category = "image",
            iconEmoji = "📄",
            defaultPrompt = "قم باستخراج وتفريغ كافة النصوص المكتوبة في هذه الوثيقة أو الصورة بدقة وتنسيقها بشكل منظم:",
            inputHint = "اطلب استخراج النصوص وتنسيقها أو ترجمتها...",
            sampleInputs = listOf(
                "استخراج وتنسيق النص المكتوب في صفحة الكتاب المصورة",
                "قراءة وتفريغ بيانات الفاتورة أو الإيصال في جدول منظم",
                "استخراج كود برمجي من لقطة شاشة وتحويله إلى نص قابل للنسخ",
                "تفريغ نصوص وثيقة رسمية مع تدقيق الأخطاء الإملائية"
            )
        )
    )

    // 🎵 قسم الصوت والموسيقى وهندسة الصوتيات وأدواته
    val audioTools = listOf(
        ServiceToolItem(
            id = "audio_tts",
            title = "استوديو القراءة الصوتية (Text-to-Speech)",
            subtitle = "تحويل أي نص أو مقال إلى قراءة صوتية عربية واضحة بنبرات طبيعية",
            category = "audio",
            iconEmoji = "🔊",
            defaultPrompt = "أريد تحويل هذا النص إلى أسلوب إلقاء صوتي جذاب ومتقن للنطق وقراءته صوتياً:",
            inputHint = "اكتب أو الصق النص الذي تود أن يلقيه ريمو بصوته العربي...",
            sampleInputs = listOf(
                "قراءة مقدمة كتاب تحفيزي عن الإصرار وتحقيق الأهداف",
                "إلقاء خاطرة أدبية شعرية دافئة بلغة عربية فصحى",
                "قراءة نشرة إخبارية تقنية سريعة وموجزة",
                "ملخص تعليمي مركز لشرح مفهوم الذكاء الاصطناعي"
            )
        ),
        ServiceToolItem(
            id = "audio_voice",
            title = "المحادثة الصوتية المباشرة (Voice Chat)",
            subtitle = "التحدث الصوتي المباشر مع ريمو والاستماع لردوده دون كتابة",
            category = "audio",
            iconEmoji = "🎙️",
            defaultPrompt = "أهلاً ريمو، أريد أن نتحدث ونتحاور صوتياً حول هذا الموضوع:",
            inputHint = "اختر الموضوع الذي تريد بدء محادثة صوتية حية حوله...",
            sampleInputs = listOf(
                "حوار صوتي تفاعلي لتدريب مهارات التحدث والتعبير",
                "محادثة صوتية للمساعدة في تنظيم جدول المهام اليومي",
                "جلسة عصف ذهني صوتية لتوليد أفكار مشاريع تقنية",
                "نقاش صوتي حول مستقبل الذكاء الاصطناعي في الوطن العربي"
            )
        ),
        ServiceToolItem(
            id = "audio_lyrics",
            title = "تأليف كلمات الأغاني وتلحين الموسيقى",
            subtitle = "كتابة قصائد وأغاني موزونة مع توصيات المقامات الموسيقية والتوزيع",
            category = "audio",
            iconEmoji = "🎵",
            defaultPrompt = "ألف لي كلمات أغنية أو قصيدة موزونة حول هذا الموضوع مع اقتراح المقام الموسيقي والإيقاع والتوزيع الصوتي:",
            inputHint = "اكتب موضوع الأغنية، النمط، والمشاعر التي تود التعبير عنها...",
            sampleInputs = listOf(
                "كلمات أغنية حماسية عن الطموح والإنجاز مع اقتراح مقام الكرد",
                "قصيدة وجدانية بالفصحى عن الوفاء والصداقة مع اقتراح مقام البيات",
                "أنشودة أطفال مبهجة عن حب التعلم والاكتشاف بإيقاع مرح",
                "كلمات أغنية عصرية بنمط البوب والموسيقى البديلة"
            )
        ),
        ServiceToolItem(
            id = "audio_podcast",
            title = "هندسة سيناريو وحلقات البودكاست",
            subtitle = "إعداد نصوص إذاعية متكاملة مع فقرات الحوار والتوزيع الصوتي",
            category = "audio",
            iconEmoji = "🎧",
            defaultPrompt = "اكتب لي سكريبت حلقة بودكاست متكاملة بمحاورها وفقراتها ومؤثراتها الصوتية المقترحة حول:",
            inputHint = "اكتب عنوان البودكاست، المدة المقترحة، والجمهور المستهدف...",
            sampleInputs = listOf(
                "سكريبت حلقة بودكاست عن ثورة الذكاء الاصطناعي التوليدي ومستقبل العمل",
                "حلقة بودكاست قصصية مشوقة عن أعظم الابتكارات الملهمة عبر التاريخ",
                "حوار بودكاست احترافي بين رائد أعمال ومستثمر في الشركات الناشئة",
                "حلقة بودكاست تأملية قصيرة للاسترخاء والصفاء الذهني قبل النوم"
            )
        )
    )

    // 📚 مقالات وشروحات تفصيلية عن أدوات الذكاء الاصطناعي Gemini
    val geminiArticles = listOf(
        GeminiArticle(
            id = "flash",
            title = "نموذج Gemini 1.5 Flash: سرعة المعالجة الفائقة والكفاءة اللحظية",
            tag = "السرعة والكفاءة اللحظية",
            iconEmoji = "⚡",
            summary = "تم تصميم نموذج Gemini 1.5 Flash ليكون خفيف الوزن وفائق السرعة، مما يجعله الخيار الأمثل للمحادثات التفاعلية والردود الحية.",
            content = """
                يعد نموذج Gemini 1.5 Flash نقلة نوعية في كفاءة معالجة الذكاء الاصطناعي. تم تدريبه عبر عملية تقطير معرفي (Knowledge Distillation) متطورة من النموذج الأكبر، مما منحه سرعة استجابة مذهلة في أجزاء من الثانية دون التضحية بالدقة.

                أبرز مزايا Gemini 1.5 Flash:
                • زمن استجابة لحظي (Sub-second Latency): يتيح إجراء محادثات طبيعية وانسيابية دون أي تأخير ملحوظ، مما يجعله المحرك الأنسب لمساعد ريمو التفاعلي.
                • معالجة متعددة الوسائط خفيفة: يستطيع قراءة وتحليل الصور والنصوص واستخراج البيانات في وقت قياسي.
                • استهلاك حصص أمثل: يتيح للمستخدمين الاستفادة من أقصى قدر من الاستفسارات اليومية بكفاءة عالية.
                • توليد الأكواد السريع: يقدم حلولاً فورية للاستفسارات البرمجية الشائعة وتوليد الدوال السريعة دون انتظار.
            """.trimIndent()
        ),
        GeminiArticle(
            id = "pro",
            title = "نموذج Gemini 1.5 Pro: نافذة السياق المليونية والتحليل المعقد",
            tag = "الذكاء المعقد والتحليل",
            iconEmoji = "🧠",
            summary = "النموذج الأكثر تطوراً للمهام التحليلية المعقدة، مع نافذة سياق رائدة عالمياً تصل إلى 2 مليون رمز (Token).",
            content = """
                يمثل Gemini 1.5 Pro قمة الذكاء التوليدي والمنطقي. يتميز بنافذة سياق غير مسبوقة في تاريخ الذكاء الاصطناعي تستوعب حتى مليوني رمز، مما يعني قدرته على قراءة وتحليل مكتبات برمجية كاملة وكتب علمية ضخمة دفعة واحدة.

                قدرات نموذج Pro المتقدمة:
                • تحليل المشاريع البرمجية الكاملة (Codebase Analysis): يمكنك تزويد النموذج بمستودع كود كامل ليفهم المعمارية ويكتشف الثغرات الدقيقة بين مختلف الطبقات.
                • الاستنتاج الرياضي والمنطقي المعقد: تفكيك المعضلات العلمية المعقدة وبناء خوارزميات برمجية مركبة.
                • استرجاع المعلومات الخارقة (Needle in a Haystack): استرجاع معلومة وحيدة مخفية بين مئات الآلاف من أسطر النصوص بدقة تقارب 99.7%.
                • التوليد الأدبي والتحرير الاحترافي: صياغة أبحاث ومقالات وتقارير استراتيجية بأسلوب لغوي راقٍ وبلاغة عالية.
            """.trimIndent()
        ),
        GeminiArticle(
            id = "vision",
            title = "أداة الرؤية الحاسوبية المتعددة الوسائط (Multimodal Vision)",
            tag = "الرؤية وفحص الصور",
            iconEmoji = "👁️",
            summary = "فهم متقدم للمشاهد البصرية، قراءة المخططات الهندسية، واستخراج النصوص الدقيقة (OCR).",
            content = """
                تقنية Multimodal Vision تمنح الذكاء الاصطناعي أعيناً رقمية فائقة الدقة. لا تقتصر الأداة على التعرف على الأشياء، بل تفهم العلاقات المكانية والفيزيائية بين العناصر داخل الصورة.

                تطبيقات الرؤية البصرية في REMO AI:
                • استخراج النصوص (OCR) بدقة متناهية: تحويل المستندات المصورة والفواتير واللوحات إلى نصوص رقمية منظمة تدعم الخط العربي واللاتيني.
                • تحليل الرسوم البيانية والمخططات: قراءة الجداول المعقدة ومخططات الـ UML واستخراج الإحصائيات والمعادلات منها.
                • فحص واجهات المستخدم (UI/UX Review): فحص لقطات شاشة التطبيقات واقتراح تحسينات في التصميم وتناسق الألوان وتجربة المستخدم.
                • المساعدة البصرية وحل المسائل: حل المعادلات الرياضية المكتوبة باليد وشرح مكونات الدوائر الإلكترونية.
            """.trimIndent()
        ),
        GeminiArticle(
            id = "code",
            title = "أداة هندسة الأكواد والمنطق البرمجي (Code Reasoning)",
            tag = "التطوير والبرمجة",
            iconEmoji = "💻",
            summary = "توليد كود إنتاجي نظيف، تحسين الأداء (Refactoring)، وتصحيح الأخطاء المعمارية لأكثر من 20 لغة.",
            content = """
                أداة Code Reasoning في نماذج Gemini مدربة على مليارات الأسطر من الكود المصدري الموثوق ومشاريع المصدر المفتوح المعتمدة عالمياً، مما يمنحها إدراكاً عميقاً لأنماط التصميم وأفضل الممارسات.

                ما تقدمه الأداة للمطورين:
                • هيكلة الكود النظيف (Clean Code Architecture): توليد دوال وكلاسات تتبع مبادئ SOLID و Clean Architecture في مختلف التقنيات كـ Android و Web و Backend.
                • التصحيح المنطقي المتقدم (Logical Debugging): لا تكتفي باكتشاف الأخطاء الصياغية (Syntax Errors)، بل تكتشف أخطاء التسريب في الذاكرة (Memory Leaks) وحالات السباق (Race Conditions).
                • تحويل وترجمة الأكواد: تحويل المشاريع القديمة (Legacy Code) إلى تقنيات معاصرة مثل تحويل Java إلى Kotlin أو JavaScript إلى TypeScript.
                • هندسة قواعد البيانات واستعلامات SQL: كتابة استعلامات عالية السرعة، تصميم الفهارس، ومنع ثغرات الأمان.
            """.trimIndent()
        ),
        GeminiArticle(
            id = "grounding",
            title = "أداة البحث الموثق الحي في الويب (Web Grounding)",
            tag = "التوثيق والبحث الحي",
            iconEmoji = "🌐",
            summary = "ربط الذكاء الاصطناعي بمحركات البحث العالمية لضمان الدقة وتفادي الهلوسة مع ذكر المصادر والروابط.",
            content = """
                تعتبر ميزة Web Grounding السلاح الأقوى ضد الهلوسة الرقمية (Hallucination). من خلال دمج قدرات البحث اللحظية، يستطيع ريمو الوصول إلى أحدث المعلومات العالمية وتزويد المستخدم بإجابات مدعومة بالمراجع والروابط المباشرة.

                فوائد البحث الموثق:
                • معلومات محدثة لحظة بلحظة: مواكبة آخر الأخبار والتقنيات والأوراق البحثية الصادرة اليوم.
                • الشفافية وتوثيق المصادر: يتم إرفاق عناوين المواقع والروابط الرسمية في نهاية كل رد ليتسنى للمستخدم التأكد من المعلومة.
                • دقة الحقائق والأرقام: التحقق من التواريخ والإحصائيات الاقتصادية والأسعار من مصادرها الرسمية.
                • التدقيق المتقاطع (Cross-Verification): مقارنة نتائج متعددة لتقديم ملخص موضوعي ومحايد وشامل.
            """.trimIndent()
        )
    )
}

data class GeminiArticle(
    val id: String,
    val title: String,
    val tag: String,
    val iconEmoji: String,
    val summary: String,
    val content: String
)

@Composable
fun ServiceToolDialog(
    tool: ServiceToolItem,
    onDismiss: () -> Unit,
    onLaunch: (String) -> Unit
) {
    var userInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = tool.iconEmoji, fontSize = 28.sp)
                Column {
                    Text(
                        text = tool.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = GoldPrimary
                    )
                    Text(
                        text = tool.subtitle,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        lineHeight = 15.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (tool.sampleInputs.isNotEmpty()) {
                    Text(
                        text = "أو اختر أحد النماذج الجاهزة:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GoldPrimary
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        tool.sampleInputs.forEach { sample ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { userInput = sample }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = sample,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "حدد تفاصيل طلبك للأداة:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    placeholder = { Text(tool.inputHint, fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onLaunch(userInput) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("تنفيذ الأداة الآن 🚀", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = Color.Gray)
            }
        }
    )
}
