package com.example.distractfree

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class FocusAccessibilityService : AccessibilityService() {

    private val blockedWords = listOf("sex", "porn", "fuck", "nude", "boobs", "xhamster", "xxx", "naked")

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.source == null) return

        val rootNode = rootInActiveWindow ?: return

        // =================== Instagram Reels ===================
        if (event.packageName == "com.instagram.android") {
            if (shouldBlockInstagramReels() && isOnInstagramReelsScreen(rootNode)) {
                clickInstagramHomeTab(rootNode)
            }
        }

        // =================== YouTube Shorts ====================
        if (event.packageName == "com.google.android.youtube") {
            if (shouldBlockYouTubeShorts() && event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                val clickedNode = event.source
                if (clickedNode != null && isShortsTab(clickedNode)) {
                    Log.d("FocusApp", "Shorts tab clicked. Redirecting to Home.")
                    clickYouTubeHomeTab(rootNode)
                }
            }
        }

        // =================== Explicit Word Typing (native views) ====================
        if (shouldBlockExplicitWords() &&
            event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED &&
            event.text != null
        ) {
            val typedText = event.text.joinToString("").lowercase()
            if (blockedWords.any { typedText.contains(it) }) {
                Toast.makeText(this, "This content is restricted.", Toast.LENGTH_SHORT).show()

                // Clear the input field text
                clearTextInEditableNode(event.source)

                // Close the app
                performGlobalAction(GLOBAL_ACTION_HOME)

                Log.d("FocusApp", "Blocked typed word: $typedText - Input cleared and app closed.")
            }

        }

        // =================== Explicit Words on Screen (Chrome/Brave/etc) ====================
        if (shouldBlockExplicitWords() &&
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            (event.packageName == "com.android.chrome" || event.packageName == "com.brave.browser")
        ) {
            val screenText = StringBuilder()
            extractAllText(rootNode, screenText)
            val visibleText = screenText.toString().lowercase()

            if (blockedWords.any { visibleText.contains(it) }) {
                Toast.makeText(this, "Inappropriate content detected. App closed.", Toast.LENGTH_SHORT).show()
                performGlobalAction(GLOBAL_ACTION_HOME)
                Log.d("FocusApp", "Blocked visible word on browser: ${event.packageName}")
            }
        }
    }

    override fun onInterrupt() {}

    private fun clearTextInEditableNode(node: AccessibilityNodeInfo?) {
        if (node == null) return

        val editableNodes = mutableListOf<AccessibilityNodeInfo>()

        var currentNode: AccessibilityNodeInfo? = node
        while (currentNode != null) {
            if (currentNode.isEditable) {
                editableNodes.add(currentNode)
            }
            currentNode = currentNode.parent
        }

        editableNodes.add(node) // in case source itself is editable

        for (editableNode in editableNodes) {
            try {
                val args = Bundle()
                args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "")
                val success = editableNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
                Log.d("FocusApp", "Tried clearing text in editable node. Success: $success")
            } catch (e: Exception) {
                Log.e("FocusApp", "Failed to clear text: ${e.message}")
            }
        }
    }



    override fun onServiceConnected() {
        val info = AccessibilityServiceInfo()
        info.eventTypes =
            AccessibilityEvent.TYPE_VIEW_CLICKED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.notificationTimeout = 100
        serviceInfo = info
    }

    // =================== Preferences ====================

    private fun shouldBlockInstagramReels(): Boolean {
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        return prefs.getBoolean("block_instagram_reels", false)
    }

    private fun shouldBlockYouTubeShorts(): Boolean {
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        return prefs.getBoolean("block_youtube_shorts", false)
    }

    private fun shouldBlockExplicitWords(): Boolean {
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        return prefs.getBoolean("block_explicit_words", false)
    }

    // =================== Instagram ====================

    private fun isOnInstagramReelsScreen(rootNode: AccessibilityNodeInfo): Boolean {
        return findNodeByText(rootNode, "Reels") != null
    }

    private fun clickInstagramHomeTab(rootNode: AccessibilityNodeInfo) {
        val homeNode = findNodeByContentDescription(rootNode, "Home")
        if (homeNode != null && homeNode.isClickable) {
            homeNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d("FocusApp", "Instagram: Clicked on Home tab.")
        } else {
            Log.d("FocusApp", "Instagram: Home tab not found or not clickable.")
        }
    }

    // =================== YouTube ====================

    private fun isShortsTab(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        return node.text?.toString()?.equals("Shorts", ignoreCase = true) == true ||
                node.contentDescription?.toString()?.equals("Shorts", ignoreCase = true) == true
    }

    private fun clickYouTubeHomeTab(rootNode: AccessibilityNodeInfo) {
        val homeNode = findNodeByContentDescription(rootNode, "Home")
        if (homeNode != null && homeNode.isClickable) {
            homeNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d("FocusApp", "YouTube: Clicked on Home tab.")
        } else {
            Log.d("FocusApp", "YouTube: Home tab not found or not clickable.")
        }
    }

    // =================== Text Scanning Utility ====================

    private fun extractAllText(node: AccessibilityNodeInfo?, builder: StringBuilder) {
        if (node == null) return

        node.text?.let { builder.append(it).append(" ") }
        node.contentDescription?.let { builder.append(it).append(" ") }

        for (i in 0 until node.childCount) {
            extractAllText(node.getChild(i), builder)
        }
    }

    private fun findNodeByText(root: AccessibilityNodeInfo?, text: String): AccessibilityNodeInfo? {
        if (root == null) return null
        if (text.equals(root.text?.toString(), ignoreCase = true)) return root
        for (i in 0 until root.childCount) {
            val child = root.getChild(i)
            val result = findNodeByText(child, text)
            if (result != null) return result
        }
        return null
    }

    private fun findNodeByContentDescription(root: AccessibilityNodeInfo?, description: String): AccessibilityNodeInfo? {
        if (root == null) return null
        if (description.equals(root.contentDescription?.toString(), ignoreCase = true)) return root
        for (i in 0 until root.childCount) {
            val child = root.getChild(i)
            val result = findNodeByContentDescription(child, description)
            if (result != null) return result
        }
        return null
    }
}
