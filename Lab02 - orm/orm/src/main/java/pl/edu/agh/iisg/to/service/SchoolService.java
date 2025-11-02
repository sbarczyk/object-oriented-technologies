package pl.edu.agh.iisg.to.service;

import pl.edu.agh.iisg.to.dao.CourseDao;
import pl.edu.agh.iisg.to.dao.GradeDao;
import pl.edu.agh.iisg.to.dao.StudentDao;
import pl.edu.agh.iisg.to.model.Course;
import pl.edu.agh.iisg.to.model.Grade;
import pl.edu.agh.iisg.to.model.Student;
import pl.edu.agh.iisg.to.repository.StudentRepository;
import pl.edu.agh.iisg.to.session.TransactionService;

import java.util.*;

public class SchoolService {

    private final TransactionService transactionService;
    private final StudentDao studentDao;
    private final CourseDao courseDao;
    private final GradeDao gradeDao;
    private final StudentRepository studentRepository;

    public SchoolService(TransactionService transactionService,
                         StudentDao studentDao,
                         CourseDao courseDao,
                         GradeDao gradeDao,
                         StudentRepository studentRepository) {
        this.transactionService = transactionService;
        this.studentDao = studentDao;
        this.courseDao = courseDao;
        this.gradeDao = gradeDao;
        this.studentRepository = studentRepository;
    }

    public boolean enrollStudent(final Course course, final Student student) {
        return transactionService.doAsTransaction(() -> {
            if (course.studentSet().contains(student)) {
                return false;
            }
            course.studentSet().add(student);
            student.courseSet().add(course);
            return true;
        }).orElse(false);
    }

    public boolean removeStudent(int indexNumber) {
        return transactionService.doAsTransaction(() -> {
            Optional<Student> optionalStudent = studentDao.findByIndexNumber(indexNumber);
            if (optionalStudent.isEmpty()) {
                return false;
            }

            Student student = optionalStudent.get();
            studentRepository.remove(student);
            return true;
        }).orElse(false);
    }

    public boolean gradeStudent(final Student student, final Course course, final float gradeValue) {
        return transactionService.doAsTransaction(() -> {
            Grade grade = new Grade(student, course, gradeValue);
            student.gradeSet().add(grade);
            course.gradeSet().add(grade);
            gradeDao.save(grade);
            return true;
        }).orElse(false);
    }

    public Map<String, List<Float>> getStudentGrades(String courseName) {
        Optional<Course> optionalCourse = courseDao.findByName(courseName);
        if (optionalCourse.isEmpty()) {
            return Collections.emptyMap();
        }

        Course course = optionalCourse.get();

        return transactionService.doAsTransaction(() -> {
            Map<String, List<Float>> report = new HashMap<>();

            List<Student> students = studentRepository.findAllByCourseName(courseName);

            for (Student student : students) {
                List<Float> grades = new ArrayList<>();

                for (Grade grade : student.gradeSet()) {
                    if (grade.course().equals(course)) {
                        grades.add(grade.grade());
                    }
                }

                Collections.sort(grades);
                report.put(student.fullName(), grades);
            }

            return report;
        }).orElse(Collections.emptyMap());
    }
}