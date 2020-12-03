package com.ukrainianboyz.nearly.utils.worker

import android.content.Context
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class WorkerUtil {
    companion object {
        inline fun <reified T : ListenableWorker> enqueueOneTimeWork(context: Context, data: Data) {
            val request = OneTimeWorkRequestBuilder<T>()
                .setInputData(data)
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}