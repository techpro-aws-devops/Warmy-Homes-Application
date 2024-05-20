package com.project.service.business;

import com.project.entity.business.Advert;
import com.project.entity.business.Image;
import com.project.exception.CustomImageNotFoundException;
import com.project.exception.DatabaseOperationException;
import com.project.exception.ResourceNotFoundException;
import com.project.payload.messages.ErrorMessages;
import com.project.repository.business.AdvertRepository;
import com.project.repository.business.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.project.entity.enums.ImageType;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final AdvertService advertService;
    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

    //I-01 /images/:imageId-get Bir reklamın görüntüsünü alacak
    public Image getImageById(Long imageId) {
        return imageRepository.findById(imageId).orElseThrow(() ->
                new ResourceNotFoundException(String.format(ErrorMessages.IMAGE_NOT_FOUND_MESSAGE, imageId)));
    }




    public List<Long> uploadImages(List<MultipartFile> images,Long advertId) {
        List<Long> uploadedImageIds = new ArrayList<>();

        for (MultipartFile imageFile : images) {
            try {
                byte[] imageData = imageFile.getBytes();
                Image image = new Image();
                image.setData(imageData);
                image.setName(imageFile.getOriginalFilename());

                String dosyaAdi = imageFile.getOriginalFilename();
                int uzantiBaslangicIndex = dosyaAdi.lastIndexOf(".");
                String uzanti = dosyaAdi.substring(uzantiBaslangicIndex + 1).toLowerCase();


                if (uzanti.equalsIgnoreCase("jpg") || uzanti.equalsIgnoreCase("jpeg")) {
                    image.setType(ImageType.JPEG);
                } else if (uzanti.equalsIgnoreCase("png")) {
                    image.setType(ImageType.PNG);
                } else if (uzanti.equalsIgnoreCase("gif")) {
                    image.setType(ImageType.GIF);
                } else  if(uzanti.equalsIgnoreCase("image")){
                    image.setType(ImageType.IMAGE);
                }else {
                    throw new IllegalStateException("Resmin tipi belirlenemedi."); // Hata mesajı ile birlikte istisna fırlatma
                }


                image.setFeatured(false); // Öne çıkan özelliği belirleme


                Advert advert=advertService.isAdvert(advertId);

                image.setAdvert(advert);

                // Burada ilgili reklama ait olan image'ı kaydedin
                Image savedImage = imageRepository.save(image);
                uploadedImageIds.add(savedImage.getId());
            } catch (IOException e) {
                // Resim yükleme sırasında bir hata oluşursa burada işlenebilir
                // Loglama veya istisna fırlatma gibi işlemler yapılabilir
                String errorMessage = "Resim yükleme sırasında bir hata oluştu: " + e.getMessage();
                System.err.println(errorMessage);
                e.printStackTrace();
            }
        }

        return uploadedImageIds;
    }

    //I-03 /images/:image_ids-delete
    public void deleteImages(List<Long> imageIds) {
        for (Long imageId : imageIds) {
            if (imageId != null) { // ID'nin null olup olmadığını kontrol ediyoruz
                Optional<Image> imageOptional = imageRepository.findById(imageId);
                if (imageOptional.isPresent()) { // Veritabanında bu ID'ye sahip bir resim var mı kontrol ediyoruz
                    imageRepository.deleteById(imageId); // Resmi sil
                } else {
                    throw new ResourceNotFoundException(String.format(ErrorMessages.IMAGE_NOT_FOUND_MESSAGE, imageId));
                }
            }
        }
    }



    // I-04 /images/:imageId-put
    public void setFeaturedImage(Long imageId) {
        if (imageId == null) {
            throw new IllegalArgumentException("Image ID cannot be null.");
        }

        try {
            Optional<Image> optionalImage = imageRepository.findById(imageId);
            if (optionalImage.isPresent()) {
                Image image = optionalImage.get();
                image.setFeatured(true);
                imageRepository.save(image);
            } else {
                throw new ResourceNotFoundException("Image not found with id: " + imageId);
            }
        } catch (ResourceNotFoundException ex) {
            logger.error("Image not found with id: " + imageId, ex);
            throw new CustomImageNotFoundException("Image not found with id: " + imageId, ex);
        } catch (DataAccessException ex) {
            logger.error("Error while accessing database", ex);
            throw new DatabaseOperationException("Error while accessing database", ex);
        }
    }


//    public List<Image> findWithAdvert(Long id) {
//        List<Image> images = imageRepository.findWithAdvert(id);
//
//        return images;
//    }
}
