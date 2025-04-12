<div dir="rtl" style="text-align: right;">

# 🔄 Redo-History Transaction Recovery Simulator (Java)

این برنامه یک شبیه‌ساز ساده برای پیاده‌سازی الگوریتم **Crash Recovery** به سبک **Redo-History Paradigm** در سیستم‌های تراکنشی است.  
کاربر می‌تواند با وارد کردن دستوراتی مانند `BEGIN`, `WRITE`, `COMMIT`, `FLUSH`, `CRASH`، سناریوهایی مشابه مثال‌های کلاسیک مثل ARIES را ایجاد و بازیابی کند.

---

## 🛠 ویژگی‌ها

- پشتیبانی از چند تراکنش همزمان
- شبیه‌سازی کش (Cache) و دیتابیس پایدار (Stable Database)
- اجرای الگوریتم‌های `Redo Pass` و `Undo Pass` بعد از کرش
- خواندن دستورات از ورودی (stdin) در قالب تعاملی
- نمایش وضعیت پایگاه داده بعد از هر Crash Recovery

---

## ▶️ نحوه اجرا

1. کد را در یک فایل جاوا ذخیره کنید، مثلاً `RedoHistoryRecoveryInteractive.java`.
2. آن را کامپایل و اجرا کنید:

```bash
javac RedoHistoryRecoveryInteractive.java
java RedoHistoryRecoveryInteractive
```

3. دستورات را وارد کنید.

## 🧾 فرمت دستورات پشتیبانی‌شده

| دستور     | فرمت                              | توضیح                                 |
|-----------|-----------------------------------|----------------------------------------|
| `BEGIN`   | `BEGIN <TXN_ID>`                  | شروع یک تراکنش جدید                   |
| `WRITE`   | `WRITE <TXN_ID> <PAGE> <DATA>`    | نوشتن داده روی یک صفحه توسط یک تراکنش |
| `COMMIT`  | `COMMIT <TXN_ID>`                 | اتمام موفق تراکنش                    |
| `FLUSH`   | `FLUSH <PAGE>`                    | نوشتن دادهٔ کش‌شده به دیسک           |
| `CRASH`   | `CRASH`                           | شبیه‌سازی خرابی و اجرای Recovery     |
| `EXIT`    | `EXIT`                            | پایان برنامه                          |

## 🧪 نمونه استفاده

```
> BEGIN T1
> WRITE T1 A val1
> WRITE T1 B val2
> COMMIT T1
> FLUSH A
> BEGIN T2
> WRITE T2 C val3
> CRASH
> EXIT
```

## 🟢 خروجی نهایی:
```
🚨 SYSTEM CRASH

🔁 Redo Pass:
🔄 Redoing: [2] WRITE T1 A val1
🔄 Redoing: [3] WRITE T1 B val2
🔄 Redoing: [6] WRITE T2 C val3

↩️ Undo Pass:
⛔ Undoing transaction T2
🧹 Undo: removing C

✅ Recovery complete. Current database: {A=val1, B=val2}

```

## 📌 نکات قابل گسترش
* اضافه‌کردن Compensation Log Entry (CLE)

* پشتیبانی از ذخیره و بارگذاری فایل‌های لاگ

* نمایش گرافیکی روند Redo/Undo

* اعمال NextUndoSeqNo برای جلوگیری از اجرای تکراری عملیات معکوس
