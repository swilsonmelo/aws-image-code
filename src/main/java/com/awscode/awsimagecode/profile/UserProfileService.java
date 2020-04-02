package com.awscode.awsimagecode.profile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.model.Bucket;
import com.awscode.awsimagecode.bucket.BucketName;
import com.awscode.awsimagecode.filestore.FileStore;

@Service
public class UserProfileService {
	
	private final UserProfileDataAccessService userProfileDataAccessService;
	
	private final FileStore fileStore;
	
	@Autowired
	public UserProfileService (UserProfileDataAccessService userProfileDataAccessService,
								FileStore fileStore) {
		this.userProfileDataAccessService = userProfileDataAccessService;
		this.fileStore = fileStore;
	}
	
	List<UserProfile> getUserProfiles(){
		return userProfileDataAccessService.getUserProfiles();
	}

	public void uploadUserProfileImage(UUID userProfileId, MultipartFile file) {
		//1. check if image is not empty
		isFileEmpty(file);
		
		//2. check if file is an a image
		isImage(file);
		
		//3. the user exist in our database
		UserProfile user = getUserProfileOrThrow(userProfileId);
		
		//4. Grap some metada from file any
		Map<String, String> metadata = extractMetadata(file);
		
		//5. Store the image in s3 and update database with s3 image link
		String path = String.format("%s/%s", BucketName.PROFILE_IMAGE.getBucketName(), user.getUserProfileId());
		String fileName = String.format("%s-%s", file.getOriginalFilename(), UUID.randomUUID());
		
		try {
			fileStore.save(path, fileName, Optional.of(metadata), file.getInputStream());
			user.setUserProfileImageLink(fileName);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	
	public byte[] downloadUserProfileImage(UUID userProfileId) {
		UserProfile user = getUserProfileOrThrow(userProfileId);
		String path = String.format("%s/%s", 
				BucketName.PROFILE_IMAGE.getBucketName(), 
				user.getUserProfileId());
		return user.getUserProfileImageLink()
				.map( key -> fileStore.download(path, key) )
				.orElse(new byte[0]);				
	}
	
	private Map<String, String> extractMetadata(MultipartFile file) {
		Map<String, String> metadata = new HashMap<String, String>();
		metadata.put("Content-Type", file.getContentType());
		metadata.put("Content-Length", String.valueOf(file.getSize()));
		return metadata;
	}

	private UserProfile getUserProfileOrThrow(UUID userProfileId) {
		return userProfileDataAccessService
			.getUserProfiles()
			.stream()
			.filter(userProfile -> userProfile.getUserProfileId().equals(userProfileId))
			.findFirst()
			.orElseThrow(() -> new IllegalStateException(String.format("User profile %s not found", userProfileId)));
	}

	private void isImage(MultipartFile file) {
		if(! Arrays.asList(
				ContentType.IMAGE_JPEG.getMimeType(), 
				ContentType.IMAGE_PNG.getMimeType()).contains(file.getContentType()) ) {
			throw new IllegalStateException("File must be an image");
		}
	}

	private void isFileEmpty(MultipartFile file) {
		if(file.isEmpty()) {
			throw new IllegalStateException("Cannot upload empty file [ "+ file.getSize() + "]" );
		}
	}

	
	
}
