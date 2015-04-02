package com.ayuget.redface.data.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Topic implements Parcelable {

    private int id;

    /**
     * The "slug" is the url identifier associated with the topic
     */
    private String slug;

    private String subject;

    private String author;

    private int pagesCount;

    private int postsCount;

    private Date lastPostDate;

    private String lastPostAuthor;

    private TopicStatus status;

    /**
     * A sticky topic will appear at the top of the category
     */
    private boolean isSticky;

    private boolean isLocked;

    private Category category;

    private int lastReadPostPage;

    private long lastReadPostId;

    private boolean hasUnreadPosts;

    public Topic(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getPagesCount() {
        return pagesCount;
    }

    public void setPagesCount(int pagesCount) {
        this.pagesCount = pagesCount;
    }

    public int getPostsCount() {
        return postsCount;
    }

    public void setPostsCount(int postsCount) {
        this.postsCount = postsCount;
    }

    public Date getLastPostDate() {
        return lastPostDate;
    }

    public void setLastPostDate(Date lastPostDate) {
        this.lastPostDate = lastPostDate;
    }

    public String getLastPostAuthor() {
        return lastPostAuthor;
    }

    public void setLastPostAuthor(String lastPostAuthor) {
        this.lastPostAuthor = lastPostAuthor;
    }

    public TopicStatus getStatus() {
        return status;
    }

    public void setStatus(TopicStatus status) {
        this.status = status;
    }

    public boolean isSticky() {
        return isSticky;
    }

    public void setSticky(boolean isSticky) {
        this.isSticky = isSticky;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    public int getUnreadPagesCount() {
        return pagesCount - lastReadPostPage;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public boolean hasUnreadPosts() {
        return hasUnreadPosts;
    }

    public void setHasUnreadPosts(boolean hasUnreadPosts) {
        this.hasUnreadPosts = hasUnreadPosts;
    }

    public int getLastReadPostPage() {
        return lastReadPostPage;
    }

    public void setLastReadPostPage(int lastReadPostPage) {
        this.lastReadPostPage = lastReadPostPage;
    }

    public long getLastReadPostId() {
        return lastReadPostId;
    }

    public void setLastReadPostId(long lastReadPostId) {
        this.lastReadPostId = lastReadPostId;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Topic{");
        sb.append("id=").append(id);
        sb.append(", subject='").append(subject).append('\'');
        sb.append(", author='").append(author).append('\'');
        sb.append('}');
        return sb.toString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.slug);
        dest.writeString(this.subject);
        dest.writeString(this.author);
        dest.writeInt(this.pagesCount);
        dest.writeInt(this.postsCount);
        dest.writeLong(lastPostDate != null ? lastPostDate.getTime() : -1);
        dest.writeString(this.lastPostAuthor);
        dest.writeInt(this.status == null ? -1 : this.status.ordinal());
        dest.writeByte(isSticky ? (byte) 1 : (byte) 0);
        dest.writeByte(isLocked ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.category, 0);
        dest.writeInt(this.lastReadPostPage);
        dest.writeLong(this.lastReadPostId);
        dest.writeByte(hasUnreadPosts ? (byte) 1 : (byte) 0);
    }

    private Topic(Parcel in) {
        this.id = in.readInt();
        this.slug = in.readString();
        this.subject = in.readString();
        this.author = in.readString();
        this.pagesCount = in.readInt();
        this.postsCount = in.readInt();
        long tmpLastPostDate = in.readLong();
        this.lastPostDate = tmpLastPostDate == -1 ? null : new Date(tmpLastPostDate);
        this.lastPostAuthor = in.readString();
        int tmpStatus = in.readInt();
        this.status = tmpStatus == -1 ? null : TopicStatus.values()[tmpStatus];
        this.isSticky = in.readByte() != 0;
        this.isLocked = in.readByte() != 0;
        this.category = in.readParcelable(Category.class.getClassLoader());
        this.lastReadPostPage = in.readInt();
        this.lastReadPostId = in.readLong();
        this.hasUnreadPosts = in.readByte() != 0;
    }

    public static final Creator<Topic> CREATOR = new Creator<Topic>() {
        public Topic createFromParcel(Parcel source) {
            return new Topic(source);
        }

        public Topic[] newArray(int size) {
            return new Topic[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Topic topic = (Topic) o;

        if (hasUnreadPosts != topic.hasUnreadPosts) return false;
        if (id != topic.id) return false;
        if (isLocked != topic.isLocked) return false;
        if (isSticky != topic.isSticky) return false;
        if (lastReadPostId != topic.lastReadPostId) return false;
        if (lastReadPostPage != topic.lastReadPostPage) return false;
        if (pagesCount != topic.pagesCount) return false;
        if (postsCount != topic.postsCount) return false;
        if (author != null ? !author.equals(topic.author) : topic.author != null) return false;
        if (category != null ? !category.equals(topic.category) : topic.category != null)
            return false;
        if (lastPostAuthor != null ? !lastPostAuthor.equals(topic.lastPostAuthor) : topic.lastPostAuthor != null)
            return false;
        if (lastPostDate != null ? !lastPostDate.equals(topic.lastPostDate) : topic.lastPostDate != null)
            return false;
        if (slug != null ? !slug.equals(topic.slug) : topic.slug != null) return false;
        if (status != topic.status) return false;
        if (!subject.equals(topic.subject)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (slug != null ? slug.hashCode() : 0);
        result = 31 * result + subject.hashCode();
        result = 31 * result + (author != null ? author.hashCode() : 0);
        result = 31 * result + pagesCount;
        result = 31 * result + postsCount;
        result = 31 * result + (lastPostDate != null ? lastPostDate.hashCode() : 0);
        result = 31 * result + (lastPostAuthor != null ? lastPostAuthor.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (isSticky ? 1 : 0);
        result = 31 * result + (isLocked ? 1 : 0);
        result = 31 * result + (category != null ? category.hashCode() : 0);
        result = 31 * result + lastReadPostPage;
        result = 31 * result + (int) (lastReadPostId ^ (lastReadPostId >>> 32));
        result = 31 * result + (hasUnreadPosts ? 1 : 0);
        return result;
    }
}
