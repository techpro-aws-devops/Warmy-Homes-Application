package com.project.repository.business;

import com.project.entity.business.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {


//    @Query(value = "SELECT id FROM t_image WHERE advert_id = ?", nativeQuery = true)
//    List<Image> findWithAdvert(Long id);
}
