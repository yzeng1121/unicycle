import { 
  View, 
  Text, 
  StyleSheet, 
  TouchableOpacity, 
  ScrollView, 
  Image, 
  ActivityIndicator, 
  Alert
} from 'react-native';
import { useState, useEffect } from 'react';
import { useAuth } from '../contexts/auth-context';
import * as ImagePicker from 'expo-image-picker';
import { router } from 'expo-router';

interface ProfileResponse {
  profileId: string;
  userId: string;
  username: string;
  profileImage: string;
  firstName: string;
  lastName: string;
  dorm: string;
  followers: string[];
  followerCount: number;
  following: string[];
  followingCount: number;
  rating: number;
  listings: string[];
  purchases: string[];
  savedListings: string[];
}

interface Profile {
  profileId: string;
  userId: string;
  username: string;
  profileImage: string;
  firstName: string;
  lastName: string;
  dorm: string;
  followers: User[];
  followerCount: number;
  following: User[];
  followingCount: number;
  rating: number;
  listings: Listing[];
  purchases: Listing[];
  savedListings: Listing[];
}

interface User {
  id: string;
  username: string;
  profilePhoto: string;
}

interface Listing {
  id: string;
  image: string;
}


const LoadingScreen: React.FC = () => (
  <View style={styles.loadingContainer}>
    <ActivityIndicator size="large" color="#007AFF" />
    <Text style={styles.loadingText}>Loading...</Text>
  </View>
);

// TODO: when user pulls down... should prompt refresh of profile screen (same with every other tab)
const profile = () => {
  const { isAuthenticated, user, makeAuthenticatedRequest, accessToken } = useAuth();

  const [loading, setLoading] = useState(true)
  const [activeTab, setActiveTab] = useState('SHOP');
  const [myProfile, setMyProfile] = useState<Profile | null>(null);
  const [uploading, setUploading] = useState(false);

  const fillMyProfile = async (profileData: ProfileResponse) => {
    let profile: Profile = {
      ...profileData,
      followers: [],
      following: [],
      listings: [],
      purchases: [],
      savedListings: [],
    };

    // run in parallel
    await Promise.all([
      fillWithListings(profile.listings, profileData.listings),
      fillWithListings(profile.purchases, profileData.purchases),
      fillWithListings(profile.savedListings, profileData.savedListings),

      fillWithUsers(profile.followers, profileData.followers),
      fillWithUsers(profile.following, profileData.following)
    ]);

    setMyProfile(profile);
  }

  const fillWithUsers = async(users: User[], idList: string[]) => {
    if (!idList || idList.length === 0) return;
    
    for (const userId of idList) {
      const imageUrl = await fetchUserProfilePicture(userId);
      const name = await fetchUsername(userId);
      let user: User = {
        id: userId,
        username: name,
        profilePhoto: (imageUrl == null || imageUrl.trim().length === 0) ? '/Users/yuxin/Desktop/unicycle_v2/apps/assets/images/placeholder-pfp.jpg' : imageUrl
      };
      users.push(user);
    }
  }

  const fetchUserProfilePicture = async (userId: string) => {
    try {
      const response = await makeAuthenticatedRequest(
        `http://Yuxins-Mac.local:8080/users/${userId}/get-profile-image`
      );

      if (response.ok) {
        const data = await response.json();
        return data.imageUrl;
      }
    } catch (error) {
      console.log("Error fetching profile photo: " + error);
    }
    return "";
  }

  const fetchUsername = async (userId: string) => {
    try {
      const response = await makeAuthenticatedRequest(
        `http://Yuxins-Mac.local:8080/users/${userId}/get-username`
      );

      if (response.ok) {
        const username = await response.json();
        return username;
      }
    } catch (error) {
      console.log("Error fetching profile photo: " + error);
    }
    return "";
  }

  const fillWithListings = async(listings: Listing[], idList: string[]) => {
    if (!idList || idList.length === 0) return;

    for (const listingId of idList) {
      const image = await fetchListingCover(listingId);
      let listing: Listing = {
        id: listingId,
        image: image
      };
      listings.push(listing);
    }
  }

  const fetchListingCover = async (listingId: string) => {
    try {
      const response = await makeAuthenticatedRequest(
          `http://Yuxins-Mac.local:8080/api/listings/${listingId}/get-cover-photo`
      );
      console.log(`Listing ${listingId} response status:`, response.status);

      if (response.ok) {
        const data = await response.json();
        console.log(`Response data for ${listingId}:`, data);
        return data.imageUrl;
      } else {
        console.log(`Failed to fetch listing ${listingId}: ${response.status}`);
      }
    } catch (error) {
      console.log("Error fetching cover photo: " + error);
    }
    return "";
  }

  useEffect(() => {
    const fetchMyProfile = async () => {
      console.log("=== Starting profile fetch ===");
      try {
        setLoading(true);

        const response = await makeAuthenticatedRequest(
          'http://Yuxins-Mac.local:8080/profiles/me'
        );

        if (response.ok) {
          const profileData: ProfileResponse = await response.json();
        
          await fillMyProfile(profileData);

          setLoading(false);
        } else {
          setLoading(false);
        }
      } catch (error) {
        console.error('Error fetching profile:', error);
        setLoading(false);
      }
    }
    fetchMyProfile();
  }, []);

  // TODO: loading screen 
  if (loading) {
    return <LoadingScreen/>;
  }

  // TODO: used for checking other peoples profile and accessing own
  if (!isAuthenticated) {
    return <Text>Please log in</Text>;
  }

  if (!user) {
    console.error("User not found.")
    return <Text>Please log in</Text>;
  }
  
  // TODO: load user's data in from either the cache or from the database
  // TODO: fetch from database (ATP if first time user, shouldve been declared beforehand)
  // TODO: detect if its me... go to the /me endpoint (e.g. accessing my own account from another instagram account)

  const tabs = ['SHOP', 'PURCHASES', 'SAVED'];

  const renderTabContent = () => {
    switch (activeTab) {
      case 'SHOP':
        console.log(`=== RENDERING SHOP TAB ===`);
        console.log(`myProfile exists:`, !!myProfile);
        console.log(`myProfile.listings:`, myProfile?.listings);
        console.log(`myProfile.listings length:`, myProfile?.listings?.length);
        return (
          <View style={styles.gridContainer}>
            {myProfile?.listings && myProfile.listings.length > 0 ? (
              myProfile.listings.map((listing, index) => (
                <TouchableOpacity 
                  key={index} 
                  style={styles.gridItem}
                  onPress={() => router.push(`../product/${listing.id}`)}
                >
                  <Image 
                    source={{ uri: listing.image }} 
                    style={styles.listingImage}
                  />
                </TouchableOpacity>
              ))
            ) : (
              <View style={styles.emptyState}>
                <Text style={styles.emptyText}>No listings yet</Text>
                <Text style={styles.emptySubtext}>Start selling to see your items here</Text>
              </View>
            )}
          </View>
        );

      case 'PURCHASES':
        return (
          <View style={styles.gridContainer}>
            {myProfile?.purchases && myProfile.purchases.length > 0 ? (
              myProfile.purchases.map((purchase, index) => (
                <View key={index} style={styles.gridItem}>
                  <Image 
                    source={{ uri: purchase.image }} 
                    style={styles.listingImage}
                  />
                </View>
              ))
            ) : (
              <View style={styles.emptyState}>
                <Text style={styles.emptyText}>No purchases yet</Text>
                <Text style={styles.emptySubtext}>Items you buy will appear here</Text>
              </View>
            )}
          </View>
        );

      case 'SAVED':
        return (
          <View style={styles.gridContainer}>
            {myProfile?.savedListings && myProfile.savedListings.length > 0 ? (
              myProfile.savedListings.map((saved, index) => (
                <View key={index} style={styles.gridItem}>
                  <Image 
                    source={{ uri: saved.image }} 
                    style={styles.listingImage}
                  />
                </View>
              ))
            ) : (
              <View style={styles.emptyState}>
                <Text style={styles.emptyText}>No saved items</Text>
                <Text style={styles.emptySubtext}>Save items you're interested in</Text>
              </View>
            )}
          </View>
        );

      default:
        return null;
    }
  };

  const handleProfileImagePress = async () => {
    console.log("Handling profile image press...")
    const permissionResult = await ImagePicker.requestMediaLibraryPermissionsAsync();
    
    if (permissionResult.granted === false) {
      Alert.alert('Permission Required', 'Permission to access camera roll is required!');
      return;
    }

    Alert.alert(
      'Update Profile Picture',
      'Choose an option',
      [
        { text: 'Camera', onPress: openCamera },
        { text: 'Photo Library', onPress: openImageLibrary },
        { text: 'Remove Existing', onPress: removeExistingImage, style: 'destructive' },
        { text: 'Cancel', style: 'cancel' }
      ]
    );
  };

  const openCamera = async () => {
    console.log("Opening iPhone camera...")
    try {
      // Request camera permission
      const { status } = await ImagePicker.requestCameraPermissionsAsync();
      if (status !== 'granted') {
        Alert.alert('Permission Required', 'We need camera permissions to take photos.');
        return;
      }

      // Launch camera
      const result = await ImagePicker.launchCameraAsync({
        mediaTypes: ['images'],
        allowsEditing: true,
        aspect: [1, 1],
        quality: 0.8,
      });

      if (!result.canceled && result.assets[0]) {
        const imageUri = result.assets[0].uri;
        console.log('Photo URI:', imageUri);
        // Use the image!
        uploadProfileImage(result.assets[0]);
      }
    } catch (error) {
      console.log('Error taking photo:', error);
      Alert.alert('Error', 'Something went wrong while taking the photo.');
    }
  };

  const openImageLibrary = async () => {
    console.log("Opening photo library...")
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ['images'],
      allowsEditing: true,
      aspect: [1, 1],
      quality: 0.8,
    });

    if (!result.canceled) {
      uploadProfileImage(result.assets[0]);
    }
  };

  const uploadProfileImage = async (imageAsset: any) => {
    console.log("Trying to upload/update profile image...")
    try {
      setUploading(true);
      
      const formData = new FormData();
      formData.append('profileImage', {
        uri: imageAsset.uri,
        type: imageAsset.type || imageAsset.mimeType,
        name: imageAsset.fileName || imageAsset.name
      } as any);

      console.log("Sending image to backend...")
      console.log("profileId = " + myProfile?.profileId)
      console.log("accessToken = " + accessToken)
      const response = await fetch(`http://Yuxins-Mac.local:8080/profiles/${myProfile?.profileId}/update/profile-image`, {
        method: 'PATCH',
        headers: {
          'Authorization': `Bearer ${accessToken}`
        },
        body: formData,
      });

      if (response.ok) {
        const updatedProfile = await response.json();
        setMyProfile(updatedProfile);
        Alert.alert('Success', 'Profile picture updated!');
      }
    } catch (error) {
      Alert.alert('Error', 'Failed to upload image');
    } finally {
      setUploading(false);
    }
  };

  const removeExistingImage = async () => {
    try {
      console.log("Attempting to delete the existing profile image...")
      const response = await fetch(`http://Yuxins-Mac.local:8080/profiles/${myProfile?.profileId}/delete/profile-image`, { // tufts
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${accessToken}`
        }
      });

      if (response.ok) { 
        const updatedProfile = await response.json();
        setMyProfile(updatedProfile);
      }
    } catch (error) {
      console.log("Error deleting exisiting profile image: " + error);
    }
  };

  return (
    <ScrollView style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.headerTitle}>{myProfile?.username || "User not found"}</Text>
      </View>

      {/* Profile Info Section */}
      <View style={styles.profileSection}>
        {/* TODO: make it so that there's a default profile image in DB */}
        <View style={styles.profileHeader}>

          {/* Top row: Profile pic and name/stats */}
          <View style={styles.topRow}>
            
            {/* Profile Image */}
            <TouchableOpacity 
              style={styles.profilePicContainer}
              onPress={handleProfileImagePress}
            >
              {myProfile?.profileImage ? (
                <Image 
                  source= {{ uri: myProfile.profileImage }}
                  style={styles.profilePicImage}
                  onError={() => {
                    console.log('Failed to load profile image');
                    console.log('URL that failed:', myProfile.profileImage);
                  }}
                />
              ) : (
                 <View style={styles.profilePic}>
                  <View style={styles.profileIcon} />
                </View>
              )}
            </TouchableOpacity>
            
            {/* TODO: when press on profile image, can change it */}

            <View style={styles.rightSection}>

              {/* Stats above name */}
              <View style={styles.statsContainer}>
                <View style={styles.statItem}>
                  <Text style={styles.statNumber}>{myProfile?.followerCount?.toString() || '0'}</Text>
                  <Text style={styles.statLabel}>followers</Text>
                </View>
                <View style={styles.statItem}>
                  <Text style={styles.statNumber}>{myProfile?.followingCount?.toString() || '0'}</Text>
                  <Text style={styles.statLabel}>following</Text>
                </View>
                <View style={styles.statItem}>
                  <Text style={styles.statNumber}>{myProfile?.rating?.toString() || ''}</Text>
                  <Text style={styles.statLabel}>rating</Text>
                </View>
              </View>

              {/* Name below stats */}
              <View style={styles.nameContainer}>
                <Text style={styles.fullName}>
                  {`${myProfile?.firstName || ''} ${myProfile?.lastName || ''}`}
                </Text>
              </View>

              <View style={styles.dormContainer}>
                <Text style={styles.dorm}>
                  {"📍 " + (myProfile?.dorm || '')}
                </Text>
              </View>


            </View>
          </View>
        </View>
      </View>

      {/* Active Listings Header */}

      {/* Tab Navigation */}
      <View style={styles.tabContainer}>
        {tabs.map((tab) => (
          <TouchableOpacity
            key={tab}
            style={[styles.tab, activeTab === tab && styles.activeTab]}
            onPress={() => setActiveTab(tab)}
          >
            <Text style={[styles.tabText, activeTab === tab && styles.activeTabText]}>
              {tab}
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* Tab Content */}
      {renderTabContent()}
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  header: {
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingTop: 60,
    paddingBottom: 20,
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#333',
  },
  reviewButton: {
    backgroundColor: '#007AFF',
    paddingHorizontal: 15,
    paddingVertical: 8,
    borderRadius: 20,
  },
  reviewButtonText: {
    color: 'white',
    fontSize: 14,
    fontWeight: '500',
  },
  profileSection: {
    paddingHorizontal: 20,
    paddingBottom: 20,
  },
  profileHeader: {
    marginBottom: 15,
  },
  topRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
  },
  profilePicContainer: {
    marginRight: 20,
  },
  rightSection: {
    flex: 1,
  },
  profilePicImage: {
    width: 80,
    height: 80,
    borderRadius: 40,
    borderWidth: 1,
    borderColor: '#ddd',
  },
  profilePic: {
    width: 80,
    height: 80,
    borderRadius: 40,
    backgroundColor: '#f0f0f0',
    justifyContent: 'center',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#ddd',
  },
  profileIcon: {
    width: 30,
    height: 30,
    borderRadius: 15,
    backgroundColor: '#ccc',
  },
  nameContainer: {
    marginTop: 15,
  },
  fullName: {
    fontSize: 16,
    marginLeft: 10,
    fontWeight: '600',
    marginTop: 8,
    textAlign: 'left',
  },
  dormContainer: {
    marginTop: 10,
    marginBottom: 15,
  },
  dorm: {
    fontSize: 14,
    marginLeft: 10,
    textAlign: 'left',
  },
  statsContainer: {
    marginTop: 15,
    flexDirection: 'row',
    flex: 1,
    justifyContent: 'space-around',
  },
  statItem: {
    alignItems: 'center',
  },
  statNumber: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
  },
  statLabel: {
    fontSize: 14,
    color: '#666',
    marginTop: 2,
  },
  username: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
  },
  sectionHeader: {
    paddingHorizontal: 20,
    paddingVertical: 15,
    borderTopWidth: 1,
    borderTopColor: '#eee',
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
  },
  tabContainer: {
    flexDirection: 'row',
    borderBottomWidth: 1,
    borderBottomColor: '#eee',
    paddingHorizontal: 20,
  },
  tab: {
    flex: 1,
    paddingVertical: 15,
    alignItems: 'center',
  },
  activeTab: {
    borderBottomWidth: 2,
    borderBottomColor: '#333',
  },
  tabText: {
    fontSize: 14,
    fontWeight: '500',
    color: '#666',
  },
  activeTabText: {
    color: '#333',
    fontWeight: '600',
  },
  gridContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    paddingHorizontal: 1,
    paddingTop: 0,      
    backgroundColor: '#fff'
  },
  gridItem: {
    width: '33.333%', 
    aspectRatio: 1,
    padding: 0.5
  },
  listingImage: {
    width: '100%',
    height: '100%',
    borderRadius: 0
  },
  placeholder: {
    flex: 1,
    backgroundColor: '#f5f5f5',
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#ddd',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f5f5f5',
  },
  loadingText: {
    marginTop: 16,
    fontSize: 16,
    color: '#666',
    fontFamily: 'SpaceMono',
  },
  emptyState: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingVertical: 40,
  },
  emptyText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#666',
    marginBottom: 5,
  },
  emptySubtext: {
    fontSize: 14,
    color: '#999',
    textAlign: 'center',
  },
});

export default profile;

