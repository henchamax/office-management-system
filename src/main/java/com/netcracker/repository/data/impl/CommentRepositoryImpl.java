package com.netcracker.repository.data.impl;

import com.netcracker.model.entity.Comment;
import com.netcracker.model.entity.Person;
import com.netcracker.model.entity.Request;
import com.netcracker.repository.common.GenericJdbcRepository;
import com.netcracker.repository.data.interfaces.CommentRepository;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CommentRepositoryImpl extends GenericJdbcRepository<Comment, Long> implements CommentRepository {

    public static final String COMMENT_ID_COLUMN = "comment_id";
    public static final String BODY = "body";
    public static final String REQUEST_ID = "request_id";
    public static final String AUTHOR_ID = "author_id";
    public static final String PUBLISH_DATE = "publish_date";

    private final String FIND_COMMENTS_BY_REQUEST_ID = "SELECT comment_id, body, request_id, author_id, publish_date FROM comment WHERE request_id=?";

    public CommentRepositoryImpl() {
        super(Comment.TABLE_NAME, Comment.ID_COLUMN);
    }

    @Override
    public Map<String, Object> mapColumns(Comment entity) {
        Map<String, Object> columns = new HashMap<>();
        columns.put(COMMENT_ID_COLUMN, entity.getId());
        columns.put(BODY, entity.getBody());
        columns.put(REQUEST_ID, entity.getRequest().getId());
        columns.put(AUTHOR_ID, entity.getAuthor().getId());
        columns.put(PUBLISH_DATE, entity.getPublishDate());
        return columns;
    }

    @Override
    public RowMapper<Comment> mapRow() {
        return new RowMapper<Comment>() {
            @Override
            public Comment mapRow(ResultSet resultSet, int i) throws SQLException {
                Comment comment = new Comment();
                comment.setId(resultSet.getLong(COMMENT_ID_COLUMN));
                comment.setBody(resultSet.getString(BODY));
                comment.setAuthor(new Person(resultSet.getLong(AUTHOR_ID)));
                comment.setRequest(new Request(resultSet.getLong(REQUEST_ID)));
                comment.setPublishDate(resultSet.getDate(PUBLISH_DATE));
                return comment;
            }
        };
    }

    @Override
    public List<Comment> findCommentByRequestId(Long requestId) {
        return super.queryForList(FIND_COMMENTS_BY_REQUEST_ID, requestId);
    }
}