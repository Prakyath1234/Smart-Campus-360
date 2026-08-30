# ATS Resume Analyzer Engine

This document details the architecture, calculations, normalization mappings, and heuristics used by the local, deterministic, and explainable ATS Resume Analyzer.

> [!IMPORTANT]
> **This is an explainable heuristic ATS compatibility score, not a prediction of any specific employer's ATS behavior.** It acts as a deterministic gap-analysis utility to highlight alignment against stated job requirements.

---

## 1. Engine Architecture

The analysis workflow executes completely locally without external LLM API calls:
```
Structured Resume (ParsedResume)
       +
Job Description (Raw Text)
       ↓
AtsAnalysisService (Skill Extraction, Normalization & Segment Matchers)
       ↓
Scoring Engine (Weighted Category Aggregations)
       ↓
Explainable ATS Result (Scoring Breakdown, Gaps & Recommendations)
```

---

## 2. Scoring Formula

The overall score is computed out of `100` points using a transparent weighted breakdown:

| Category | Max Score | Description |
|---|---|---|
| **Skills Match** | 40 | Evaluates matches against Required (70%) and Preferred (30%) skills |
| **Experience Match** | 25 | Compares required years in JD vs candidate detected years |
| **Education Match** | 10 | Matches required minimum degree ranking against candidate degrees |
| **Keyword Coverage** | 15 | Measures occurrence of top 20 frequent unique JD terms in resume |
| **Project Match** | 10 | Checks if resume projects use JD required/preferred skills |
| **Total** | **100** | **Comprehensive compatibility index** |

---

## 3. Skill Normalization

Equivalent technologies are normalized to prevent scoring penalties:
* `springboot`, `spring boot` → **Spring Boot**
* `reactjs`, `react.js`, `ReactJS` → **React**
* `js`, `javascript` → **JavaScript**
* `postgres`, `postgresql` → **PostgreSQL**
* `k8s`, `kubernetes` → **Kubernetes**
* `nodejs`, `node.js` → **Node.js**
* `angularjs`, `angular.js` → **Angular**

### Collision Protection
* **Java** does NOT match **JavaScript**.
* **C** does NOT match **C++** or **C#** (guaranteed via boundary-safe lookaheads: `(?i)(?:^|\\s|\\p{Punct})C(?![+#])(?:$|\\s|\\p{Punct})`).

---

## 4. Required vs Preferred Classification
- The engine splits JD text into segments.
- Sections prefixed with words like "required", "must have", "requirements", or "minimum qualifications" place subsequent skills under the **Required** classification.
- Sections prefixed with "preferred", "nice to have", "plus", or "desired" place subsequent skills under the **Preferred** classification.
- If no classification can be reliably resolved, skills default to **UNKNOWN** and are treated with equal scoring weight.

---

## 5. Matcher Implementations

### Experience Matcher
- Extracts target years from JD using pattern matching (e.g. "3+ years", "5 years").
- Sums candidate duration intervals from experience list entries.
- If experience matches/exceeds requirement, full 25 points are awarded. Otherwise, partial points are assigned scale-proportionally.

### Education Matcher
- Ranks degrees hierarchically: `PhD` (3) > `Master's` (2) > `Bachelor's` (1) > `UNKNOWN` (0).
- Compares JD requirements against resume listings, awarding full 10 points for met/exceeded criteria, and 5 points for lower matches.

### Keyword Coverage
- Filters out non-technical stop words.
- Extracts top 20 unique technical keywords from JD.
- Counts unique occurrences in resume to compute the match score (repeated keyword spamming does not inflate scores).

---

## 6. Recommendations Engine
Generates deterministic suggestions based on gaps:
- Missing required skills: *"Add [Skill Name] if you possess it, as it is required."*
- Experience gaps: *"Demonstrate measurable outcomes to support your years of experience."*
- Keyword gaps: *"Use matching terminology from the job description when describing your previous roles."*

---

## 7. Limitations
- Does not scan images or locked PDFs (relying on structured text extraction).
- Evaluates keywords based on spelling variants (un-mapped custom abbreviations might not normalize).
