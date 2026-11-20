# Loan Calculator Android App

বাংলা-সমর্থিত Android Personal Loan Calculator-এর সম্পূর্ণ Android Studio প্রজেক্ট।

## কী কী আছে
- চাকরির ক্যাটাগরি নির্বাচন
- নেট বেতন, বয়স, সর্বোচ্চ বয়স, Salary Multiple
- DBR ও বিদ্যমান EMI
- Loan amount, annual interest rate, tenure
- Flat ও Reducing EMI হিসাব
- সর্বোচ্চ সম্ভাব্য লোনের প্রাথমিক হিসাব
- বয়স অনুযায়ী মেয়াদ সীমা
- EMI, মোট সুদ, মোট পরিশোধ ও DBR
- প্রথম ১২ মাসের কিস্তি Schedule

## APK তৈরি করার নিয়ম
1. কম্পিউটারে Android Studio ইনস্টল করুন।
2. Android Studio থেকে **Open** নির্বাচন করে এই প্রজেক্টের ফোল্ডার খুলুন।
3. Gradle Sync সম্পন্ন হতে দিন (ইন্টারনেট লাগবে)।
4. **Build > Build Bundle(s) / APK(s) > Build APK(s)** নির্বাচন করুন।
5. APK সাধারণত `app/build/outputs/apk/debug/app-debug.apk` ফোল্ডারে তৈরি হবে।

## গুরুত্বপূর্ণ
- এই ZIP-এ Android Studio source project দেওয়া হয়েছে; APK binary আগে থেকে compile করা নেই।
- Gradle Wrapper অন্তর্ভুক্ত নয়। Android Studio-র Gradle support দিয়ে প্রজেক্ট খুলুন।
- Salary multiple, DBR, বয়সসীমা উদাহরণ হিসেবে দেওয়া। আপনার ব্যাংকের অনুমোদিত নীতিমালা বসিয়ে নিন।
- এটি প্রাথমিক যোগ্যতা যাচাই; ব্যাংকের চূড়ান্ত অনুমোদন নয়।
