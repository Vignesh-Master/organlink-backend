# OrganLink AI Training Datasets

This directory contains datasets for training the AI matching models.

## Dataset Structure

### Expected CSV Format for Organ Matching:

```csv
donor_age,donor_bmi,donor_blood_type,donor_smoking,donor_alcohol,patient_age,patient_bmi,patient_blood_type,patient_urgency,patient_waiting_days,blood_type_compatible,age_difference,bmi_difference,distance_km,organ_type,hla_match_score,crossmatch_compatible,match_success
35,24.5,O+,never,light,42,26.1,A+,HIGH,120,no,7,1.6,85.2,kidney,4,yes,success
28,22.3,A-,former,none,31,23.8,A-,MEDIUM,45,yes,3,1.5,12.5,liver,5,yes,success
45,27.8,B+,current,moderate,38,25.2,B+,CRITICAL,200,yes,7,2.6,156.3,heart,3,no,failure
```

## Column Descriptions:

### Donor Features:
- `donor_age`: Age of donor (18-70)
- `donor_bmi`: Body Mass Index of donor
- `donor_blood_type`: Blood type (A+, A-, B+, B-, AB+, AB-, O+, O-)
- `donor_smoking`: Smoking status (never, former, current)
- `donor_alcohol`: Alcohol consumption (none, light, moderate, heavy)

### Patient Features:
- `patient_age`: Age of patient (18-80)
- `patient_bmi`: Body Mass Index of patient
- `patient_blood_type`: Blood type (A+, A-, B+, B-, AB+, AB-, O+, O-)
- `patient_urgency`: Urgency level (LOW, MEDIUM, HIGH, CRITICAL, EMERGENCY)
- `patient_waiting_days`: Days on waiting list

### Compatibility Features:
- `blood_type_compatible`: Blood type compatibility (yes/no)
- `age_difference`: Absolute age difference
- `bmi_difference`: Absolute BMI difference
- `distance_km`: Distance between donor and patient in kilometers

### Medical Features:
- `organ_type`: Type of organ (heart, liver, kidney, lung, pancreas)
- `hla_match_score`: HLA compatibility score (0-6)
- `crossmatch_compatible`: Crossmatch test result (yes/no)

### Target Variable:
- `match_success`: Outcome (success/failure)

## How to Add Your Kaggle Dataset:

1. **Download CSV from Kaggle**
2. **Place in this directory**: `src/main/resources/datasets/`
3. **Use the training API**: 
   ```bash
   POST /api/v1/ai/training/upload-dataset
   POST /api/v1/ai/training/train-with-csv
   ```

## Supported Kaggle Datasets:

- Organ donation compatibility datasets
- Medical matching datasets
- Transplant outcome datasets
- Any CSV with donor-patient matching features

## Data Preprocessing:

The system automatically handles:
- Missing value imputation
- Categorical encoding
- Feature normalization
- Data validation

## Model Training:

- **Algorithm**: Random Forest
- **Cross-validation**: 10-fold
- **Metrics**: Accuracy, Precision, Recall, F1-Score
- **Output**: Trained model saved to `src/main/resources/models/`
