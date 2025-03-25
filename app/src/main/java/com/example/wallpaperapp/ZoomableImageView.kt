package com.example.wallpaperapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView

class ZoomableImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    // Ma trận chuyển đổi để áp dụng scale và translate
    private val matrixValues = Matrix()

    // Biến lưu trữ scale và dịch chuyển hiện tại
    private var scaleFactor = 1.0f
    private var translationX = 0f
    private var translationY = 0f

    // Để xử lý zoom
    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            // Cập nhật scale factor theo scale gesture
            scaleFactor *= detector.scaleFactor
            // Giới hạn mức zoom (ví dụ: 0.5x đến 3.0x)
            scaleFactor = scaleFactor.coerceIn(0.5f, 3.0f)
            invalidate()
            return true
        }
    })

    // Các biến để xử lý pan (kéo)
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var isPanning = false

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Xử lý zoom trước
        scaleDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                isPanning = true
            }
            MotionEvent.ACTION_MOVE -> {
                // Nếu không đang thực hiện gesture zoom (ScaleGestureDetector), cập nhật dịch chuyển
                if (!scaleDetector.isInProgress && isPanning) {
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY
                    translationX += dx
                    translationY += dy
                    invalidate()
                }
                lastTouchX = event.x
                lastTouchY = event.y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isPanning = false
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        // Lưu trạng thái canvas
        canvas.save()

        // Áp dụng scale và dịch chuyển bằng matrix
        val pivotX = width / 2f
        val pivotY = height / 2f

        // Ta có thể thực hiện translate trước, sau đó scale để đạt hiệu ứng tự nhiên
        canvas.translate(translationX, translationY)
        canvas.scale(scaleFactor, scaleFactor, pivotX, pivotY)

        // Vẽ ảnh như bình thường
        super.onDraw(canvas)
        // Khôi phục trạng thái canvas
        canvas.restore()
    }
}
