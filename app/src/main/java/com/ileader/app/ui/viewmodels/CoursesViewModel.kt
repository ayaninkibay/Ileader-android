package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.CourseDto
import com.ileader.app.data.remote.dto.CourseLessonDto
import com.ileader.app.data.remote.dto.SportDto
import com.ileader.app.data.repository.CourseRepository
import com.ileader.app.data.util.Alerts
import com.ileader.app.data.util.AppLogger
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
                AppLogger.e("CoursesListVM.load failed", e)
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

    private val _requestState = MutableStateFlow<UiState<Unit>?>(null)
    val requestState: StateFlow<UiState<Unit>?> = _requestState

    private val _requestSent = MutableStateFlow(false)
    val requestSent: StateFlow<Boolean> = _requestSent

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
                AppLogger.e("CourseDetailVM.load failed", e)
                _state.value = UiState.Error(e.message ?: "Ошибка загрузки курса")
            }
        }
    }

    fun enroll(userId: String, courseId: String) {
        viewModelScope.launch {
            _enrollState.value = UiState.Loading
            try {
                repo.enrollInFreeCourse(userId, courseId)
                Alerts.success("Вы записаны на курс")
                _enrollState.value = UiState.Success(Unit)
                // Reload to update access status
                load(courseId, userId)
            } catch (e: Exception) {
                AppLogger.e("CourseDetailVM.enroll failed", e)
                Alerts.error("Не удалось записаться на курс")
                _enrollState.value = UiState.Error(e.message ?: "Ошибка записи на курс")
            }
        }
    }

    /** Checks once on load whether the user already sent a pending request. */
    fun checkPendingRequest(courseId: String, userId: String) {
        viewModelScope.launch {
            try {
                _requestSent.value = repo.hasPendingAccessRequest(courseId, userId)
            } catch (_: Exception) { /* non-critical */ }
        }
    }

    fun submitAccessRequest(
        courseId: String,
        userId: String,
        userName: String,
        userEmail: String?,
        userPhone: String?,
        messengerType: String,
        messengerHandle: String,
        message: String?,
    ) {
        viewModelScope.launch {
            _requestState.value = UiState.Loading
            try {
                repo.createAccessRequest(
                    courseId = courseId,
                    userId = userId,
                    userName = userName,
                    userEmail = userEmail,
                    userPhone = userPhone,
                    messengerType = messengerType,
                    messengerHandle = messengerHandle,
                    message = message,
                )
                _requestSent.value = true
                _requestState.value = UiState.Success(Unit)
                Alerts.success("Заявка отправлена. Менеджер свяжется с вами")
            } catch (e: Exception) {
                AppLogger.e("CourseDetailVM.submitAccessRequest failed", e)
                _requestState.value = UiState.Error(e.message ?: "Не удалось отправить заявку")
                Alerts.error(e.message ?: "Не удалось отправить заявку")
            }
        }
    }

    fun clearRequestError() {
        if (_requestState.value is UiState.Error) _requestState.value = null
    }
}
