import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

class Student implements Serializable {
    private String id;
    private String name;
    private Map<String, Integer> subjectMarks;

    public Student(String id, String name, Map<String, Integer> subjectMarks) {
        this.id = id;
        this.name = name;
        this.subjectMarks = subjectMarks;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Map<String, Integer> getSubjectMarks() {
        return subjectMarks;
    }

    public int getTotalMarks() {
        return subjectMarks.values().stream().mapToInt(Integer::intValue).sum();
    }

    public double getAverageMarks() {
        return subjectMarks.values().stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Student ID: ").append(id).append("\n");
        sb.append("Name: ").append(name).append("\n");
        sb.append("Subjects:\n");
        subjectMarks.forEach((subject, mark) -> sb.append("  ").append(subject).append(": ").append(mark).append("\n"));
        sb.append("Total Marks: ").append(getTotalMarks()).append("\n");
        sb.append("Average: ").append(String.format("%.2f", getAverageMarks())).append("\n");
        return sb.toString();
    }
}

public class StudentManagementSystem {
    private static List<Student> students = new ArrayList<>();
    private static Set<String> studentIds = new HashSet<>();
    private static final String FILE_NAME = "students.dat";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        loadStudents();

        while (true) {
            System.out.println("\n--- Student Management Menu ---");
            System.out.println("1. Add Student");
            System.out.println("2. Search Student");
            System.out.println("3. Delete Student");
            System.out.println("4. Display All Students");
            System.out.println("5. Save Students");
            System.out.println("6. Exit");
            System.out.print("Choose: ");
            int choice = Integer.parseInt(sc.nextLine());

            switch (choice) {
                case 1 -> addStudent(sc);
                case 2 -> searchStudent(sc);
                case 3 -> deleteStudent(sc);
                case 4 -> displayAllStudents();
                case 5 -> saveStudents();
                case 6 -> {
                    saveStudents();
                    System.out.println("Exiting...");
                    return;
                }
                default -> System.out.println("Invalid option!");
            }
        }
    }

    private static void addStudent(Scanner sc) {
        System.out.print("Enter Student ID: ");
        String id = sc.nextLine().trim();
        if (studentIds.contains(id)) {
            System.out.println("ID already exists. Try again.");
            return;
        }

        System.out.print("Enter Name: ");
        String name = sc.nextLine().trim();

        Map<String, Integer> subjectMarks = new HashMap<>();
        while (true) {
            System.out.print("Enter Subject Name (or 'done' to finish): ");
            String subject = sc.nextLine().trim();
            if (subject.equalsIgnoreCase("done")) break;

            System.out.print("Enter Marks (0-100): ");
            int mark = Integer.parseInt(sc.nextLine().trim());
            if (mark < 0 || mark > 100) {
                System.out.println("Invalid marks. Must be between 0 and 100.");
                continue;
            }
            subjectMarks.put(subject, mark);
        }

        Student student = new Student(id, name, subjectMarks);
        students.add(student);
        studentIds.add(id);

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            int total = student.getTotalMarks();
            double avg = student.getAverageMarks();
            System.out.println("Total: " + total + ", Average: " + String.format("%.2f", avg));
        });

        future.join();
    }

    private static void searchStudent(Scanner sc) {
        System.out.print("Enter Name or ID to Search: ");
        String keyword = sc.nextLine().trim().toLowerCase();

        List<Student> found = students.stream()
                .filter(s -> s.getId().equalsIgnoreCase(keyword) || s.getName().toLowerCase().contains(keyword))
                .collect(Collectors.toList());

        if (found.isEmpty()) {
            System.out.println("No student found.");
        } else {
            found.forEach(System.out::println);
        }
    }

    private static void deleteStudent(Scanner sc) {
        System.out.print("Enter Student ID to Delete: ");
        String id = sc.nextLine().trim();

        Optional<Student> student = students.stream()
                .filter(s -> s.getId().equalsIgnoreCase(id))
                .findFirst();

        if (student.isPresent()) {
            students.remove(student.get());
            studentIds.remove(id);
            System.out.println("Student removed.");
        } else {
            System.out.println("Student ID not found.");
        }
    }

    private static void displayAllStudents() {
        if (students.isEmpty()) {
            System.out.println("No students to display.");
            return;
        }
        students.forEach(System.out::println);
    }

    private static void saveStudents() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME));
             PrintWriter pw = new PrintWriter("students_print.dat")) {

            oos.writeObject(students);

            for (Student s : students) {
                pw.println(s);
            }

            System.out.println("Students saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving students: " + e.getMessage());
        }
    }

    private static void loadStudents() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            students = (List<Student>) ois.readObject();
            studentIds = students.stream().map(Student::getId).collect(Collectors.toSet());
            System.out.println("Students loaded successfully.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading students: " + e.getMessage());
        }
    }
}
