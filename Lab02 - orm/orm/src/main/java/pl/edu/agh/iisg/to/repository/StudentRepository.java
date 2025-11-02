package pl.edu.agh.iisg.to.repository;

import pl.edu.agh.iisg.to.dao.GradeDao;
import pl.edu.agh.iisg.to.dao.StudentDao;
import pl.edu.agh.iisg.to.dao.CourseDao;
import pl.edu.agh.iisg.to.model.Course;
import pl.edu.agh.iisg.to.model.Grade;
import pl.edu.agh.iisg.to.model.Student;
import pl.edu.agh.iisg.to.session.TransactionService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class StudentRepository implements Repository<Student> {

    private final StudentDao studentDao;
    private final GradeDao gradeDao;
    private final CourseDao courseDao;
    private final TransactionService transactionService;

    public StudentRepository(StudentDao studentDao, GradeDao gradeDao, CourseDao courseDao, TransactionService transactionService) {
        this.studentDao = studentDao;
        this.gradeDao = gradeDao;
        this.courseDao = courseDao;
        this.transactionService = transactionService;
    }

    @Override
    public Optional<Student> add(final Student student) {
        return studentDao.save(student);
    }

    @Override
    public Optional<Student> getById(final int id) {
        return studentDao.findById(id);
    }

    @Override
    public List<Student> findAll() {
        return studentDao.findAll();
    }

    @Override
    public void remove(final Student student) {
        transactionService.doAsTransaction(() -> {
            for (Course course : student.courseSet()) {
                course.studentSet().remove(student);
            }
            student.courseSet().clear();

            for (Grade grade : student.gradeSet()) {
                gradeDao.remove(grade);
            }
            student.gradeSet().clear();

            studentDao.remove(student);

            return null;
        });
    }

    public List<Student> findAllByCourseName(String courseName) {
        return transactionService.<List<Student>>doAsTransaction(() -> {
            Optional<Course> optionalCourse = courseDao.findByName(courseName);
            if (optionalCourse.isEmpty()) {
                return Collections.<Student>emptyList();
            }

            Course course = optionalCourse.get();
            return new ArrayList<>(course.studentSet());
        }).orElseGet(Collections::emptyList);
    }


}