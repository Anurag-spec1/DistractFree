package com.example.distractfree

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class FocusAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.source == null) return

        val rootNode = rootInActiveWindow ?: return

        if (event.packageName == "com.instagram.android") {
            if (isOnReelsScreen(rootNode)) {
                if (shouldBlockInstagramReels()) {
                    clickHomeTab(rootNode)
                }
            }
        }
    }

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or AccessibilityEvent.TYPE_VIEW_CLICKED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.notificationTimeout = 100
        serviceInfo = info
    }

    private fun shouldBlockInstagramReels(): Boolean {
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        return prefs.getBoolean("block_instagram_reels", false)
    }

    private fun isOnReelsScreen(rootNode: AccessibilityNodeInfo): Boolean {
        return findNodeByText(rootNode, "Reels") != null
    }

    private fun clickHomeTab(rootNode: AccessibilityNodeInfo) {
        val homeNode = findNodeByContentDescription(rootNode, "Home")
        if (homeNode != null && homeNode.isClickable) {
            homeNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d("FocusApp", "Clicked on Home tab.")
        } else {
            Log.d("FocusApp", "Home tab not found or not clickable.")
        }
    }

    private fun findNodeByText(root: AccessibilityNodeInfo?, text: String): AccessibilityNodeInfo? {
        if (root == null) return null
        if (text.equals(root.text?.toString(), ignoreCase = true)) {
            return root
        }
        for (i in 0 until root.childCount) {
            val child = root.getChild(i)
            val result = findNodeByText(child, text)
            if (result != null) return result
        }
        return null
    }

    private fun findNodeByContentDescription(root: AccessibilityNodeInfo?, description: String): AccessibilityNodeInfo? {
        if (root == null) return null
        if (description.equals(root.contentDescription?.toString(), ignoreCase = true)) {
            return root
        }
        for (i in 0 until root.childCount) {
            val child = root.getChild(i)
            val result = findNodeByContentDescription(child, description)
            if (result != null) return result
        }
        return null
    }
}

