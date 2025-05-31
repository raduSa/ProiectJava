package Repository;

import java.sql.*;
import java.util.*;

public abstract class AbstractCrudService<T, ID> implements CrudService<T, ID> {

    protected Connection connection;

    protected AbstractCrudService(Connection connection) {
        this.connection = connection;
    }

    protected abstract T mapResultSet(ResultSet rs) throws SQLException;
    protected abstract String getInsertSQL();
    protected abstract void fillInsertStatement(PreparedStatement ps, T entity) throws SQLException;
    protected abstract String getSelectByIdSQL();
    protected abstract String getSelectAllSQL();
    protected abstract String getUpdateSQL();
    protected abstract void fillUpdateStatement(PreparedStatement ps, T entity) throws SQLException;
    protected abstract String getDeleteSQL();

    @Override
    public T create(T entity) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(getInsertSQL())) {
            fillInsertStatement(ps, entity);
            ps.executeUpdate();
        }
        return entity;
    }

    @Override
    public T read(ID id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(getSelectByIdSQL())) {
            ps.setObject(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
            return null;
        }
    }

    @Override
    public List<T> readAll() throws SQLException {
        List<T> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(getSelectAllSQL())) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    @Override
    public T update(ID id, T entity) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(getUpdateSQL())) {
            fillUpdateStatement(ps, entity);
            ps.executeUpdate();
        }
        return entity;
    }

    @Override
    public boolean delete(ID id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(getDeleteSQL())) {
            ps.setObject(1, id);
            int affected = ps.executeUpdate();
            return affected > 0;
        }
    }
}
