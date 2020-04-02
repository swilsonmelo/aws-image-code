package com.awscode.awsimagecode.datastore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.awscode.awsimagecode.profile.UserProfile;

@Repository
public class FakeUserProfileDataStore {
	
	private static final List<UserProfile> USER_PROFILES = new ArrayList<>();
	
	static {
		USER_PROFILES.add(new UserProfile(UUID.fromString("07c42937-98a2-443b-a7f2-4d9ab1e704d3"), "janetJones", null));
		USER_PROFILES.add(new UserProfile(UUID.fromString("bc10e9d4-a928-42f2-bdb5-6ca05d3ab1ee"), "Antoniojunior", null));
	}
	
	public List<UserProfile> getUserProfiles(){
		return USER_PROFILES;
	}
}
