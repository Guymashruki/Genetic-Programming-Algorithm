StockClassifierGP הוא פרויקט המבוסס על תכנות גנטי (Genetic Programming) באמצעות ספריית JGAP, שמטרתו לסווג מניות לחמש קטגוריות תנועה אפשריות על בסיס נתונים היסטוריים. האלגוריתם מנתח תכונות טכניות של המניה, לומד על דפוסים, ומייצר מודל המסוגל לחזות את תנועת המחיר הצפויה.

הסיווג כולל חמש רמות שינוי:

Significant Rise (עלייה חדה)

Mild Rise (עלייה מתונה)

No Change (ללא שינוי)

Mild Fall (ירידה מתונה)

Significant Fall (ירידה חדה)

Features

ניתוח נתוני מניות מקבצי CSV.

טיעון 5 פיצ'רים טכניים:
RSI, SMA, OBV, ATRR, RM5

בניית אוכלוסייה גנטית של 300 פרטים.

ביצוע אבולוציה לאורך 50 דורות.

שימוש באופרטורים גנטיים:
Add, Subtract, Multiply, Max, Min, Variable, Constant

פונקציית כושר המודדת דיוק סיווג.

הפקת תחזית סופית לכל קובץ מניה.

הדפסת טבלה מסכמת לתחזיות.
How It Works
1️⃣ טעינת נתוני המניה

כל קובץ .csv מכיל עמודות עם מחירים וערכים טכניים.
ה-DataLoader מחלץ את:

RSI

SMA

OBV

ATRR

RM5

אחוז שינוי המחיר (pctChange)

מה pctChange מחושב ה-label (0-4).

2️⃣ בניית מודל תכנות גנטי

המערכת מייצרת עץ GP באמצעות JGAP:

שימוש ב־functions:
Add, Subtract, Multiply, Max, Min

שימוש ב־terminals:
משתנים 0–4, קבועים בתחום [-10,10].

3️⃣ הפעלת האבולוציה

המודל עובר 50 דורות, ובכל דור נמדד:

best fitness = #correct_predictions / total

4️⃣ הפקת תחזית

בסוף הריצה האלגוריתם מחשב:

raw = program.execute_int(features)
label = raw % 5


ולאחר מכן מתרגם לקטגוריה מילולית כמו "Mild Rise".

 Output Example
======================================
Running GP on file: AAPL.csv
Samples loaded: 200
Generation  1: best fitness = 0.4230
...
Generation 50: best fitness = 0.5620

======= Final Predictions =======
Stock File           | Prediction
---------------------------------------------
AAPL.csv            | Mild Rise
TSLA.csv            | Significant Fall
MSFT.csv            | No Change
=================================

 Prerequisites

Java 8+

ספריית JGAP GP
להורדה: https://sourceforge.net/projects/jgap/

 How to Run

עדכן את הנתיב לתיקיית ה-CSV בקוד:

String folderPath = "C:\\Users\\Guy\\Desktop\\GP.Stocks";


ודא שבתיקייה יש קבצי CSV במבנה הבא:

Date,Open,High,Low,Close,Volume,RSI,SMA,OBV,ATRR,RM5,TargetClose


הידור והרצה:

javac -cp jgap.jar StockClassifierGP.java
java -cp .;jgap.jar StockClassifierGP

 Labels Interpretation
Label	Meaning	Rule on pctChange
0	Significant Rise	pct ≥ +2%
1	Mild Rise	+0.5% < pct < +2%
2	No Change	-0.5% ≤ pct ≤ +0.5%
3	Mild Fall	-2% < pct < -0.5%
4	Significant Fall	pct ≤ -2%
 Used Algorithms & Methods

Genetic Programming (tree-based)

Symbolic Regression for classification

Evolutionary population search

Label mapping באמצעות מודולו 5
