package src.pl.edu.agh.student.swieczor.rabbitmqhospital.message;

public class ResultExaminationMessage extends ExaminationMessage {
    private String examinationResult;

    public ResultExaminationMessage(ExaminationType examinationType, String patient, String examinationResult) {
        super(examinationType, patient);
        this.examinationResult = examinationResult;
    }

    public String getExaminationResult() {
        return examinationResult;
    }

    @Override
    public String toString() {
        return super.toString() + " DONE with RESULT " + examinationResult;
    }
}
