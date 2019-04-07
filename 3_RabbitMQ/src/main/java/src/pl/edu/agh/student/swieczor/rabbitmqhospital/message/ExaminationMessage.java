package src.pl.edu.agh.student.swieczor.rabbitmqhospital.message;

public abstract class ExaminationMessage extends Message {
    private ExaminationType examinationType;
    private String patient;

    public ExaminationMessage(ExaminationType examinationType, String patient) {
        super();
        this.examinationType = examinationType;
        this.patient = patient;
    }

    public ExaminationType getExaminationType() {
        return examinationType;
    }

    public String getPatient() {
        return patient;
    }

    @Override
    public String toString() {
        return examinationType.toString() + " " + patient;
    }


    public enum ExaminationType {
        KNEE,
        HIP,
        ELBOW
    }
}
