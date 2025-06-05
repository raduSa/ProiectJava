package Repository;

import java.sql.SQLException;
import java.util.List;

public interface CrudService<T, ID> {
    T create(T entity) throws SQLException;
    T read(ID id) throws SQLException;
    T update(ID id, T entity) throws SQLException;
    boolean delete(ID id) throws SQLException;
}
