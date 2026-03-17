import { useEffect, useState } from 'react';
import { 
  View, 
  Text, 
  Image, 
  ScrollView, 
  StyleSheet, 
  Dimensions, 
  TouchableOpacity,
  ActivityIndicator,
  Alert,
} from 'react-native';
import { useLocalSearchParams, router } from 'expo-router';
import { Ionicons, MaterialIcons } from '@expo/vector-icons';

import { useAuth } from '../../contexts/AuthContext';

const { width: screenWidth } = Dimensions.get('window');

// TODO: consider adding size field for clothing items?
type Listing = {
  itemId: string;
  userId: string;
  listingType: 'Selling' | 'Giveaway' | 'Trading' | 'Lending';
  title: string;
  description: string;
  createdAt: string;
  brand: string;
  condition: string;
  category: string;
  pickUpLocation: string;
  imageUrls: string[];
  price?: number;
  tradeFor?: string;
  returnBy?: string;
  pickUpBy?: string;
};

type User = {
  userId: string;
  username: string;
  firstName: string;
  lastName: string;
  profileImageUrl: string;
}

export default function ProductScreen() {
  // TODO: standardized component every item must have ... but if seller didn't
  //       input (like an empty description), set a placeholder value for each
  //       certain components must be filled in, like a name, category, listing
  //       type, etc.
  // const item = data.find(product => product.id === itemId);

  const { itemId } = useLocalSearchParams<{ itemId: string }>();
  const [item, setItem] = useState<Listing | null>(null);
  const [userHeader, setUserHeader] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [currentIndex, setCurrentIndex] = useState(0);
  const { makeAuthenticatedRequest, user } = useAuth();
  const [isOwner, setIsOwner] = useState(false);

  // TODO: get listing information
  useEffect(() => {
    const fetchProduct = async () => {
      try {
        const response = await makeAuthenticatedRequest(
          `http://13.221.95.208:8080/api/listings/${itemId}`
        );
        
        if (response.ok) {
          console.log('Item successfully fetched from database...');
          const productData: Listing = await response.json();
          
          console.log("Fetched product data:", productData);
          setItem(productData);

          // TODO: double check to make sure that user id has been set before we try fetching user data
          let userId = productData.userId;
          setIsOwner(userId === user.userId);
          console.log('userId: ' + userId);

          // TODO: fetch basic user info
          try {
            const response = await makeAuthenticatedRequest(
              `http://13.221.95.208:8080/users/${userId}/get-header`
            );
            
            if (response.ok) {
              console.log('Item successfully fetched from database...');
              const userData: User = await response.json();
              console.log("Fetched user data:", userData);
              setUserHeader(userData);
            }
          } catch (error) {
            console.error('Error fetching user of product:', error);
          }
        }
      } catch (error) {
        console.error('Error fetching product:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchProduct();
  }, [itemId]);

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#007AFF" />
        <Text>Loading listing...</Text>
      </View>
    );
  }

  // TODO: if a product is not found... should instead redirect user to a 
  // product not found screen 
  if (!item) {
    return <Text>Product not found.</Text>;
  }

  const handleScroll = (event: any) => {
    const scrollPosition = event.nativeEvent.contentOffset.x;
    const imageWidth = event.nativeEvent.layoutMeasurement.width;
    const index = Math.round(scrollPosition / imageWidth);
    setCurrentIndex(index);
  };

  const listingConfigurations = () => {
    Alert.alert(
      'Listing Options',
      'Choose an option',
      [
        { 
          text: 'Edit Listing', 
        },
        { 
          text: 'Delete Listing', 
          style: 'destructive',
          onPress: deleteListing
        },
        { 
          text: 'Cancel', 
          style: 'cancel' 
        },
      ]
    );
  }

  // TODO: implement dis
  const editListing = async () => {
    
  }

  const deleteListing = async () => {
    Alert.alert(
      'Are you sure?',
      'Choose an option',
      [
        { 
          text: 'Confirm', 
          style: 'destructive',
        },
        { 
          text: 'Cancel', 
          style: 'cancel',
          onPress: () => {
            return;
          }
        },
      ]
    );

    // TODO: should NOT be in the header bc if you somehow obtain someone elses' user ID,
    //       you can very easily manipulate and delete someone elses' listing
    try {
      const result = await makeAuthenticatedRequest(
        `http://13.221.95.208:8080/api/listings/${item.itemId}`,
        {
          method: 'DELETE'
        }
      )
    } catch (error) {
      console.error('Error deleting product:', error);
    }

  }

  return (
    <View style={styles.container}>
      {/* Header with back button and bookmark */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => router.back()} style={styles.backButton}>
          <Ionicons name="chevron-back" size={24} color="#000" />
        </TouchableOpacity>
        <TouchableOpacity style={styles.bookmarkButton}>
          <MaterialIcons name="bookmark-border" size={24} color="#000" />
        </TouchableOpacity>
      </View>

      {/* TODO: make it so that the dots on the bottom update after scrolling and
                make it so that there's only a single image per dot not all
                stitched together */}
      <ScrollView style={styles.scrollContainer}>
        {/* Image Gallery */}
        <View style={styles.imageContainer}>
          <ScrollView 
            horizontal 
            pagingEnabled 
            showsHorizontalScrollIndicator={false}
            onScroll={handleScroll}     
            scrollEventThrottle={16}
            style={styles.imageScrollView}
          >
            {item?.imageUrls?.map((imageUrl, index) => (
              <Image 
                key={index}
                source={{ uri: imageUrl }}
                style={styles.productImage}
              />
            ))}
          </ScrollView>
          
          {/* Image indicator dots - only show if we have images */}
          {item?.imageUrls && item.imageUrls.length > 1 && (
            <View style={styles.imageIndicators}>
              {item.imageUrls.map((_, index) => (
                <View 
                  key={index} 
                  style={[
                    styles.indicator, 
                    index === currentIndex ? styles.activeIndicator : styles.inactiveIndicator
                  ]} 
                />
              ))}
            </View>
          )}

          {/* Bookmark icon on image */}
          {/* <TouchableOpacity style={styles.imageBookmark}>
            <MaterialIcons name="bookmark-border" size={20} color="#666" />
          </TouchableOpacity> */}
          {isOwner ? (
            <TouchableOpacity 
              style={styles.editButton}
              onPress={listingConfigurations}
            >
              <MaterialIcons name="more-vert" size={20} color="#666" />
            </TouchableOpacity>
          ) : null}

        </View>

        {/* Product Details */}
        <View style={styles.detailsContainer}>
          {/* Item Name */}
          <Text style={styles.itemName}>{item.title}</Text>
          
          {/* Category */}
          {/* TODO: add subcategory (size, etc.) */}
          <Text style={styles.categoryText}>
            {item.category}
          </Text>

          {/* Listing Type + Info */}
          {item.listingType === 'Selling' && (
            <Text style={styles.listingTypeText}>${item.price.toFixed(2)}</Text>
          )}
          {item.listingType === 'Giveaway' && (
            <Text style={styles.listingTypeText}>Free</Text>
          )}
          {item.listingType === 'Trading' && (
            <Text style={styles.listingTypeText}>Trading</Text>
          )}
          {/* TODO: add a rate for lending feature */}
          {item.listingType === 'Lending' && (
            <Text style={styles.listingTypeText}>${item.price.toFixed(2)}</Text>
          )}

          {/* Condition */}
          <Text style={styles.conditionTitle}>CONDITION</Text>
          <Text style={styles.conditionValue}>{item.condition}</Text>

          {/* TODO: add each user's profile image rather than a default */}
          {/* Seller Profile */}
          <View style={styles.sellerContainer}>
            {/* <View style={styles.sellerIcon}>
              <Text style={styles.sellerIconText}>👤</Text>
            </View> */}
            <Image
              source={{ uri: userHeader?.profileImageUrl }}
              style={{ width: 32, height: 32, borderRadius: 16, marginRight: 10 }}
            />
            <Text>
              {userHeader?.username || `User ID: ${item.userId}`}
            </Text>
          </View>

          {/* Description */}
          <Text style={styles.descriptionText}>{item.description}</Text>

          {/* Date Posted */}
          <Text style={styles.dateText}>{item.createdAt.substring(0, 10)}</Text>
        </View>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingTop: 50,
    paddingBottom: 10,
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderBottomColor: '#000',
  },
  backButton: {
    padding: 5,
  },
  bookmarkButton: {
    padding: 5,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#fff',
  },
  scrollContainer: {
    flex: 1,
  },
  imageContainer: {
    position: 'relative',
    height: screenWidth,
    borderBottomWidth: 1,
    borderBottomColor: '#000',
  },
  imageScrollView: {
    height: 300,
  },
  productImage: {
    width: screenWidth,
    height: screenWidth,
    backgroundColor: '#f5f5f5',
  },
  imageIndicators: {
    position: 'absolute',
    bottom: 20,
    flexDirection: 'row',
    alignSelf: 'center',
  },
  indicator: {
    width: 8,
    height: 8,
    borderRadius: 4,
    marginHorizontal: 3,
  },
  activeIndicator: {
    backgroundColor: '#000',
  },
  inactiveIndicator: {
    backgroundColor: '#fff',
    borderWidth: 1,
    borderColor: '#000',
  },
  editButton: {
    position: 'absolute',
    top: 20,
    right: 20,
    backgroundColor: 'rgba(255, 255, 255, 0.8)',
    borderRadius: 15,
    padding: 5,
  },
  detailsContainer: {
    padding: 20,
    borderBottomWidth: 1,
    borderBottomColor: '#000',
  },
  itemName: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 8,
    color: '#000',
  },
  categoryText: {
    fontSize: 12,
    fontWeight: 'bold',
    color: '#000',
    letterSpacing: 1,
    marginBottom: 15,
  },
  listingTypeText: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#000',
    letterSpacing: 1,
    marginBottom: 10,
  },
  conditionTitle: {
    fontSize: 12,
    fontWeight: 'bold',
    color: '#000',
    letterSpacing: 1,
    marginBottom: 5,
  },
  conditionValue: {
    fontSize: 14,
    color: '#000',
    marginBottom: 20,
  },
  sellerContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 20,
  },
  sellerIcon: {
    width: 24,
    height: 24,
    borderRadius: 12,
    backgroundColor: '#000',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 10,
  },
  sellerIconText: {
    color: '#fff',
    fontSize: 12,
  },
  sellerUsername: {
    fontSize: 14,
    color: '#000',
  },
  descriptionText: {
    fontSize: 14,
    color: '#000',
    marginBottom: 20,
  },
  dateText: {
    fontSize: 12,
    fontWeight: 'bold',
    color: '#000',
    letterSpacing: 1,
  },
});