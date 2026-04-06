package com.example.allot.model.event;

import java.util.Date;

/**
 * Represents a comment or reply on an event.
 */
public class EventComment {
    private String commentId;
    private String authorId;
    private String authorName;
    private String text;
    private Date createdAt;
    private String parentId;
    private int upvotes;
    private int downvotes;
    private java.util.ArrayList<String> upvoterIds;
    private java.util.ArrayList<String> downvoterIds;

    /**
     * Creates a new EventComment instance.
     */
    public EventComment() {
    }

    /**
     * Creates a new EventComment instance.
     *
     * @param commentId the comment id
     * @param authorId the author id
     * @param authorName the author name
     * @param text the text
     * @param createdAt the created at
     * @param parentId the parent id
     */
    public EventComment(String commentId, String authorId, String authorName,
                        String text, Date createdAt, String parentId) {
        this.commentId = commentId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.text = text;
        this.createdAt = createdAt;
        this.parentId = parentId;
        this.upvotes = 0;
        this.downvotes = 0;
        this.upvoterIds = new java.util.ArrayList<>();
        this.downvoterIds = new java.util.ArrayList<>();
    }

    /**
     * Returns the comment id.
     *
     * @return the comment id
     */
    public String getCommentId() {
        return commentId;
    }

    /**
     * Updates the comment id.
     *
     * @param commentId the comment id
     */
    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    /**
     * Returns the author id.
     *
     * @return the author id
     */
    public String getAuthorId() {
        return authorId;
    }

    /**
     * Updates the author id.
     *
     * @param authorId the author id
     */
    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    /**
     * Returns the author name.
     *
     * @return the author name
     */
    public String getAuthorName() {
        return authorName;
    }

    /**
     * Updates the author name.
     *
     * @param authorName the author name
     */
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    /**
     * Returns the text.
     *
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * Updates the text.
     *
     * @param text the text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Returns the created at.
     *
     * @return the created at
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Updates the created at.
     *
     * @param createdAt the created at
     */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns the parent id.
     *
     * @return the parent id
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * Updates the parent id.
     *
     * @param parentId the parent id
     */
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    /**
     * Returns the upvotes.
     *
     * @return the upvotes
     */
    public int getUpvotes() {
        return upvotes;
    }

    /**
     * Updates the upvotes.
     *
     * @param upvotes the upvotes
     */
    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    /**
     * Returns the downvotes.
     *
     * @return the downvotes
     */
    public int getDownvotes() {
        return downvotes;
    }

    /**
     * Updates the downvotes.
     *
     * @param downvotes the downvotes
     */
    public void setDownvotes(int downvotes) {
        this.downvotes = downvotes;
    }

    /**
     * Returns the upvoter ids.
     *
     * @return the upvoter ids
     */
    public java.util.ArrayList<String> getUpvoterIds() {
        if (upvoterIds == null) {
            upvoterIds = new java.util.ArrayList<>();
        }
        return upvoterIds;
    }

    /**
     * Updates the upvoter ids.
     *
     * @param upvoterIds the upvoter ids
     */
    public void setUpvoterIds(java.util.ArrayList<String> upvoterIds) {
        this.upvoterIds = upvoterIds;
    }

    /**
     * Returns the downvoter ids.
     *
     * @return the downvoter ids
     */
    public java.util.ArrayList<String> getDownvoterIds() {
        if (downvoterIds == null) {
            downvoterIds = new java.util.ArrayList<>();
        }
        return downvoterIds;
    }

    /**
     * Updates the downvoter ids.
     *
     * @param downvoterIds the downvoter ids
     */
    public void setDownvoterIds(java.util.ArrayList<String> downvoterIds) {
        this.downvoterIds = downvoterIds;
    }
}
