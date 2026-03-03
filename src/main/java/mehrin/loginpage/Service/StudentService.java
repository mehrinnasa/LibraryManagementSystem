package mehrin.loginpage.Service;

import mehrin.loginpage.Model.Student;
import mehrin.loginpage.Util.FileUtil;
import java.util.ArrayList;
import java.util.List;

public class StudentService {
    private static final String STUDENTS_FILE = "students.csv";
    private static final String HEADER = "StudentID,Name,Phone,Email,Registration,Session,Year,Semester";
    private List<Student> students;

    public StudentService() {
        loadStudents();
    }

    private void loadStudents() {
        students = new ArrayList<>();
        List<String> lines = FileUtil.readFile(STUDENTS_FILE);
        for (String line : lines) {
            if (!line.isEmpty()) {
                Student student = Student.fromCSV(line);
                if (student != null) students.add(student);
            }
        }
    }

    public List<Student> getAllStudents() {
        return new ArrayList<>(students);
    }

    public Student getStudentById(String studentId) {
        for (Student student : students) {
            if (student.getStudentId().equals(studentId)) {
                return student;
            }
        }
        return null;
    }

    public boolean addStudent(Student student) {
        students.add(student);
        saveStudents();
        return true;
    }

    public boolean deleteStudent(String studentId) {
        students.removeIf(student -> student.getStudentId().equals(studentId));
        saveStudents();
        return true;
    }

    public boolean updateStudent(Student updatedStudent) {
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getStudentId().equals(updatedStudent.getStudentId())) {
                students.set(i, updatedStudent);
                saveStudents();
                return true;
            }
        }
        return false;
    }

    public List<Student> searchStudents(String query) {
        List<Student> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (Student student : students) {
            if (student.getName().toLowerCase().contains(lowerQuery) ||
                    student.getStudentId().toLowerCase().contains(lowerQuery) ||
                    student.getEmail().toLowerCase().contains(lowerQuery)) {
                results.add(student);
            }
        }
        return results;
    }

    public int getTotalStudents() {
        return students.size();
    }

    private void saveStudents() {
        List<String> lines = new ArrayList<>();
        for (Student student : students) {
            lines.add(student.toCSV());
        }
        FileUtil.writeFile(STUDENTS_FILE, lines, HEADER);
    }
}