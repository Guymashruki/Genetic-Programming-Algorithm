דוגמא פשוטה:   


import org.jgap.gp.*;
import org.jgap.gp.impl.*;
import org.jgap.*;
import org.jgap.impl.*;

import java.util.*;

public class StudentClassifierGP {

    public static class Student {
        public double studyHours;
        public int absences;
        public double previousGrade;
        public int assignmentsCompleted;

        public Student(double studyHours, int absences, double previousGrade, int assignmentsCompleted) {
            this.studyHours = studyHours;
            this.absences = absences;
            this.previousGrade = previousGrade;
            this.assignmentsCompleted = assignmentsCompleted;
        }
    }

    public static class StudentData extends GPData {
        public double value;

        @Override
        public void copyTo(final GPData gpd) {
            ((StudentData) gpd).value = value;
        }
    }

    public static class StudyHours extends CommandGene {
        public StudyHours(final GPConfiguration config) throws InvalidConfigurationException {
            super(config, 0, CommandGene.DoubleClass);
        }

        @Override
        public String toString() {
            return "studyHours";
        }

        @Override
        public void execute_void(ProgramChromosome c, int n, GPData input, ADFStack stack) {
            ((StudentData) input).value = ((StudentInput) getGPConfiguration().getUserData()).student.studyHours;
        }

        @Override
        public Class<?> getReturnType() {
            return CommandGene.DoubleClass;
        }
    }

    public static class Absences extends CommandGene {
        public Absences(final GPConfiguration config) throws InvalidConfigurationException {
            super(config, 0, CommandGene.DoubleClass);
        }

        @Override
        public String toString() {
            return "absences";
        }

        @Override
        public void execute_void(ProgramChromosome c, int n, GPData input, ADFStack stack) {
            ((StudentData) input).value = ((StudentInput) getGPConfiguration().getUserData()).student.absences;
        }

        @Override
        public Class<?> getReturnType() {
            return CommandGene.DoubleClass;
        }
    }

    public static class PreviousGrade extends CommandGene {
        public PreviousGrade(final GPConfiguration config) throws InvalidConfigurationException {
            super(config, 0, CommandGene.DoubleClass);
        }

        @Override
        public String toString() {
            return "previousGrade";
        }

        @Override
        public void execute_void(ProgramChromosome c, int n, GPData input, ADFStack stack) {
            ((StudentData) input).value = ((StudentInput) getGPConfiguration().getUserData()).student.previousGrade;
        }

        @Override
        public Class<?> getReturnType() {
            return CommandGene.DoubleClass;
        }
    }

    public static class AssignmentsCompleted extends CommandGene {
        public AssignmentsCompleted(final GPConfiguration config) throws InvalidConfigurationException {
            super(config, 0, CommandGene.DoubleClass);
        }

        @Override
        public String toString() {
            return "assignmentsCompleted";
        }

        @Override
        public void execute_void(ProgramChromosome c, int n, GPData input, ADFStack stack) {
            ((StudentData) input).value = ((StudentInput) getGPConfiguration().getUserData()).student.assignmentsCompleted;
        }

        @Override
        public Class<?> getReturnType() {
            return CommandGene.DoubleClass;
        }
    }

    public static class StudentInput {
        public Student student;

        public StudentInput(Student student) {
            this.student = student;
        }
    }

    public static void main(String[] args) throws Exception {
        // Sample student input
        Student student = new Student(6.5, 2, 85.0, 10);

        // Manually created program for demo purposes (usually GP evolves this)
        double score = student.studyHours * 1.5 + (100 - student.absences * 2) + student.previousGrade * 0.5 + student.assignmentsCompleted * 3;

        int category;
        if (score >= 220) category = 0; // excellent
        else if (score >= 180) category = 1; // good
        else if (score >= 150) category = 2; // average
        else if (score >= 120) category = 3; // poor
        else category = 4; // fail

        System.out.println("Score: " + score);
        System.out.println("Classification (0=excellent, 4=fail): " + category);
    }
}