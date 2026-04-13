package com.tam.finance_tracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tam.finance_tracker.domain.CreditCard;
import com.tam.finance_tracker.domain.User;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {
    // Chỉ cần thế này thôi, Tâm đã có sẵn các hàm save(), findAll(),
    // deleteById()...

    // Hoặc nếu Tâm muốn tìm nhanh bằng Username mà không cần load nguyên Object
    // User:
    List<CreditCard> findByUser_Username(String username);

    List<CreditCard> findByUser(User user);

    // Kiểm tra xem thẻ có thuộc quyền sở hữu của User không trước khi Xóa/Sửa
    boolean existsByIdAndUser_Username(Long id, String username);// Trong Spring Data JPA, khi Tâm dùng dấu gạch dưới
                                                                 // (_), bạn đang thực hiện Property Traversal. Điều này
                                                                 // có nghĩa là JPA sẽ tự động hiểu rằng bạn đang muốn
                                                                 // truy cập vào thuộc tính "user" của CreditCard, sau
                                                                 // đó tiếp tục truy cập vào thuộc tính "username" của
                                                                 // User để thực hiện điều kiện kiểm tra. Cụ thể, phương
                                                                 // thức này sẽ kiểm tra xem có tồn tại một CreditCard
                                                                 // với id nhất định và thuộc về một User có username
                                                                 // nhất định hay không.
}
