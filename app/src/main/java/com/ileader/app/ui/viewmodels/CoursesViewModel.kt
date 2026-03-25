package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.CourseDto
import com.ileader.app.data.remote.dto.CourseLessonDto
import com.ileader.app.data.remote.dto.SportDto
import com.ileader.app.data.repository.CourseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ══════════════════════════════════════════════════════════
// DATA CLASSES
// ══════════════════════════════════════════════════════════

data class CourseListItem(
    val course: CourseDto,
    val lessonsCount: Int = 0,
    val studentsCount: Int = 0
)

data class CoursesListData(
    val courses: List<CourseListItem>,
    val sports: List<SportDto>
)

data class CourseDetailData(
    val course: CourseDto,
    val lessons: List<CourseLessonDto>,
    val lessonsCount: Int = 0,
    val studentsCount: Int = 0,
    val hasAccess: Boolean = false
)

// ══════════════════════════════════════════════════════════
// COURSES LIST VM
// ══════════════════════════════════════════════════════════

class CoursesListViewModel : ViewModel() {
    private val repo = CourseRepository()

    private val _state = MutableStateFlow<UiState<CoursesListData>>(UiState.Loading)
    val state: StateFlow<UiState<CoursesListData>> = _state

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val courses = repo.getPublishedCourses()
                val sports = repo.getSports()

                val courseIds = courses.map { it.id }
                val lessonCounts = repo.getLessonCounts(courseIds)
                val studentCounts = repo.getStudentCounts(courseIds)

                val items = courses.map { course ->
                    CourseListItem(
                        course = course,
                        lessonsCount = lessonCounts[course.id] ?: 0,
                        studentsCount = studentCounts[course.id] ?: 0
                    )
                }

                _state.value = UiState.Success(CoursesListData(items, sports))
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки курсов")
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// COURSE DETAIL VM
// ══════════════════════════════════════════════════════════

class CourseDetailViewModel : ViewModel() {
    private val repo = CourseRepository()

    private val _state = MutableStateFlow<UiState<CourseDetailData>>(UiState.Loading)
    val state: StateFlow<UiState<CourseDetailData>> = _state

    private val _enrollState = MutableStateFlow<UiState<Unit>?>(null)
    val enrollState: StateFlow<UiState<Unit>?> = _enrollState

    fun load(courseId: String, userId: String?) {
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val course = repo.getCourseDetail(courseId)
                val lessons = repo.getCourseLessons(courseId)
                val lessonCounts = repo.getLessonCounts(listOf(courseId))
                val studentCounts = repo.getStudentCounts(listOf(courseId))
                val hasAccess = if (userId != null) {
                    repo.checkCourseAccess(courseId, userId)
                } else {
                    course.isFree
                }

                _state.value = UiState.Success(
                    CourseDetailData(
                        course = course,
                        lessons = lessons,
                        lessonsCount = lessonCounts[courseId] ?: lessons.size,
                        studentsCount = studentCounts[courseId] ?: 0,
                        hasAccess = hasAccess
                    )
                )
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки курса")
            }
        }
    }

    fun enroll(userId: String, courseId: String) {
        viewModelScope.launch {
            _enrollState.value = UiState.Loading
            try {
                repo.enrollInFreeCourse(userId, courseId)
                _enrollState.value = UiState.Success(Unit)
                // Reload to update access status
                load(courseId, userId)
            } catch (e: Exception) {
                _enrollState.value = UiState.Error(e.message ?: "Ошибка записи на курс")
            }
        }
    }
}
