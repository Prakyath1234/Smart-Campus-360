# Academic Calculator

This document details the formulas, precision standards, and mapping conventions used by the Academic Calculator (SGPA, CGPA, and What-If simulation engine).

---

## 1. Grade-Point Mapping Scheme

Grades are matched dynamically to their corresponding points:

| Grade | Points | Range | Description |
|---|---|---|---|
| **A+** / **S** | 10 | 90 - 100 | Outstanding |
| **A** | 9 | 80 - 89 | Excellent |
| **B+** | 8 | - | Very Good (Manual Entry Only) |
| **B** | 7 | 70 - 79 | Good |
| **C** | 6 | 60 - 69 | Above Average |
| **D** | 5 | 50 - 59 | Average |
| **E** | 4 | 40 - 49 | Pass |
| **F** | 0 | < 40 | Fail |

---

## 2. SGPA Formula
$$SGPA = \frac{\sum (Credits \times GradePoints)}{\sum Credits}$$

- **Weighting**: Credit points are weighted proportionally per subject (e.g. 4-credit course holds more weight than 2-credit course).
- **Rounding Mode**: `BigDecimal` divisions rounded with `RoundingMode.HALF_UP` to two decimal places (e.g. 8.66666 -> 8.67, 7.125 -> 7.13).

---

## 3. CGPA Formula
$$CGPA = \frac{\sum (SGPA \times SemesterCredits)}{\sum SemesterCredits}$$

- **Weighting**: Weighted by credits earned per semester. It is NOT calculated as a simple mathematical average of SGPAs.

---

## 4. What-If Simulator
- Simulates hypothetical grade improvements or reductions for the current semester workload.
- Calculates projected SGPA based on target grades, comparing it to the current SGPA.

---

## 5. Target CGPA Estimator
- Predicts required future SGPA targets using the formula:
$$RequiredSGPA = \frac{TargetCGPA \times (CompletedCredits + FutureCredits) - CurrentCGPA \times CompletedCredits}{FutureCredits}$$

- **Impossibility Detection**: If the required SGPA evaluates to $> 10.0$ or $< 0.0$, the result is declared mathematically impossible.
