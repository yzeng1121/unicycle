import { 
  View, 
  Text, 
  TextInput, 
  TouchableOpacity, 
  ScrollView, 
  StyleSheet, 
  Alert, 
  Image
} from 'react-native'
import React, { useState } from 'react'
import * as ImagePicker from 'expo-image-picker';
import { useAuth } from '../contexts/auth-context';

// TODO: i kinda dont want the nav bar here
// TODO: what to dispaly when image uploads to S3 but for whatever reason doesnt display

const Post = () => {
  // TODO: implement so dont need to constantly verify during API calls
  const { makeAuthenticatedRequest, accessToken, user } = useAuth();

  // const [userId, setUserId] = useState('')
  const [listingType, setListingType] = useState('')
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [brand, setBrand] = useState('')
  const [condition, setCondition] = useState('')
  const [category, setCategory] = useState('')
  const [pickUpLocation, setPickUpLocation] = useState('')
  const [images, setImages] = useState(Array(10).fill(null));
  const [price, setPrice] = useState('')
  const [tradeItem, setTradeItem] = useState('')
  const [returnDate, setReturnDate] = useState('')
  const [pickUpDate, setPickUpDate] = useState('')
  
  const [showCategoryDropdown, setShowCategoryDropdown] = useState(false)
  const [showConditionDropdown, setShowConditionDropdown] = useState(false)
  const [showExchangeDropdown, setShowExchangeDropdown] = useState(false)

  // TODO: add one called appliances
  const categories = ['Electronics', 'Books', 'Furniture', 'Appliances', 'Clothing', 'Sports', 'Tickets', 'Other']
  const conditions = ['New', 'Like New', 'Good', 'Fair', 'Poor']
  const exchangeTypes = ['Selling', 'Trading', 'Lending', 'Giveaway']

  const handleSaveToDrafts = () => {
    Alert.alert('Success', 'Item saved to drafts!')
  }

  // TODO: throw error if not enough images
  const handlePostListing = async () => {

    const imagesToUpload = images.filter(image => image !== null);
      
    if (imagesToUpload.length === 0) {
      Alert.alert('No Images', 'Please add at least one image');
      return;
    }

    console.log('handlePostListing started...');
    const formData = new FormData();

    // Or with a fallback
    let userId = user?.userId || 'null';

    // Example usage
    if (userId) {
      console.log('Current user ID:', userId);
    } else {
      console.log('Invalid User Id');
      userId = "null"
    }

    // String fields
    formData.append('userId', userId);
    formData.append('listingType', listingType);
    formData.append('title', title);
    formData.append('description', description);
    formData.append('brand', brand);
    formData.append('condition', condition);
    formData.append('category', category);
    formData.append('pickUpLocation', pickUpLocation);

    // Optional fields
    if (price != null) {
      formData.append('price', price);
    }
    if (tradeItem != null) {
      formData.append('tradeFor', tradeItem);
    }
    if (returnDate != null) {
      formData.append('returnBy', returnDate);
    }
    if (returnDate != null) {
      formData.append('pickUpBy', pickUpDate);
    }

    const validImages = images.filter(imageUri => imageUri != null && imageUri !== '');
      console.log('📸 Valid image URIs:', validImages);
      
      validImages.forEach((imageUri, index) => {
        console.log(`📸 Adding image ${index}: ${imageUri}`);
        formData.append('images', {
          uri: imageUri,  // ← imageUri is already the string, not an object
          type: 'image/jpeg',
          name: `listing_${Date.now()}_${index}.jpg`
        } as any);
    });

    // Single API call handles everything!
    try {
      const response = await makeAuthenticatedRequest('http://10.243.71.82:8080/api/listings/create', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${accessToken}`
        },
        body: formData
      });

      const responseData = await response.json();
      Alert.alert('Success', 'Item posted successfully!');
    } catch (error) {
      console.error('Error posting listing:', error);
      Alert.alert('Error', 'Failed to post listing. Please try again.');
    }
  }
  // TODO: error when not filled in all required fields
  // TODO: send images contained inside the array to AWS S3

  // TODO: Send listing data to your backend

  // TODO: since no async storage yet... hardcoded my userId

  // TODO: what if user submits a file that not of jpeg format

  // TODO: figure out how to actually upload files on a non-expo-go platform
  // TODO: have 'take photo'/'choose from gallery' UI match that of an avg iOS app
  // TODO: option to delete image in the plus boxes
  // TODO: maybe refresh the post page after we press post listing instead of keeping it the same to prevent spam
  const handleUploadingImages = (index: number) => {
    Alert.alert(
      'Add Photo',
      'Choose an option',
      [
        { 
          text: 'Take Photo', 
          onPress: () => takePhoto(index)
        },
        { 
          text: 'Choose from Gallery', 
          onPress: () => pickImage(index)
        },
        { 
          text: 'Cancel', 
          style: 'cancel' 
        },
      ]
    );
  };

  // TODO: allow users to select multiple photos which will automatically fill up the boxes
  // TODO: limit to max of 10 images
  const pickImage = async (index: number) => {
    try {
      // Request permission
      const { status } = await ImagePicker.requestMediaLibraryPermissionsAsync();
      if (status !== 'granted') {
        Alert.alert('Permission Required', 'We need camera roll permissions to select images.');
        return;
      }

      // Launch image library
      const result = await ImagePicker.launchImageLibraryAsync({
        mediaTypes: ImagePicker.MediaTypeOptions.Images,
        allowsEditing: true,
        aspect: [1, 1],
        quality: 0.8,
        allowsMultipleSelection: false,
      });

      if (!result.canceled && result.assets[0]) {
        const imageUri = result.assets[0].uri;
        console.log('Image URI:', imageUri);
        // Use the image!
        // TODO: image should cover the current image box ...
        // TODO: image should be stored in temporary array before uploading to AWS S3

        // Update the specific box
        // TODO: for modularity, make this separate func cus duplicate
        setImages(prevImages => {
          const newImages = [...prevImages];
          newImages[index] = imageUri;
          return newImages;
        });
      }
    } catch (error) {
      console.log('Error picking image:', error);
      Alert.alert('Error', 'Something went wrong while selecting the image.');
    }
  };

  const takePhoto = async (index: number) => {
    try {
      // Request camera permission
      const { status } = await ImagePicker.requestCameraPermissionsAsync();
      if (status !== 'granted') {
        Alert.alert('Permission Required', 'We need camera permissions to take photos.');
        return;
      }

      // Launch camera
      const result = await ImagePicker.launchCameraAsync({
        mediaTypes: ImagePicker.MediaTypeOptions.Images,
        allowsEditing: true,
        aspect: [1, 1],
        quality: 0.8,
      });

      if (!result.canceled && result.assets[0]) {
        const imageUri = result.assets[0].uri;
        console.log('Photo URI:', imageUri);
        // Use the image!

        // Update the specific box
        setImages(prevImages => {
          const newImages = [...prevImages];
          newImages[index] = imageUri;
          return newImages;
        });
      }
    } catch (error) {
      console.log('Error taking photo:', error);
      Alert.alert('Error', 'Something went wrong while taking the photo.');
    }
  };

  // TODO: when user pressed on an input field that would be covered by the keyboard, the input field should ideally slide up so user can see
  const renderExchangeSpecificFields = () => {
    switch(listingType) {
      case 'Selling':
        return (
          <View style={styles.exchangeField}>
            <Text style={styles.label}>PRICE</Text>
            <View style={styles.priceContainer}>
              <Text style={styles.dollarSign}>$</Text>
              <TextInput
                style={styles.priceInput}
                placeholder="0.00"
                value={price}
                onChangeText={setPrice}
                keyboardType="numeric"
              />
            </View>
          </View>
        )
      case 'Trading':
        return (
          <View style={styles.exchangeField}>
            <Text style={styles.label}>TRADING FOR</Text>
            <TextInput
              style={styles.input}
              placeholder="Item you're hoping to trade for..."
              value={tradeItem}
              onChangeText={setTradeItem}
            />
          </View>
        )
      case 'Lending':
        return (
          <View style={styles.exchangeField}>
            <Text style={styles.label}>RETURN BY</Text>
            <TextInput
              style={styles.input}
              placeholder="YYYY/MM/DD"
              value={returnDate}
              onChangeText={setReturnDate}
            />
          </View>
        )
      case 'Giveaway':
        return (
          <View style={styles.exchangeField}>
            {/* TODO: have it so that the date is a scroll wheel */}
            <Text style={styles.label}>PICK-UP BY</Text>
            <TextInput
              style={styles.input}
              placeholder="YYYY/MM/DD"
              value={pickUpDate}
              onChangeText={setPickUpDate}
            />
            <Text style={styles.label}>PICK-UP LOCATION</Text>
            <TextInput
              style={styles.input}
              placeholder="Location for pickup..."
              value={pickUpLocation}
              onChangeText={setPickUpLocation}
            />

            <Text style={styles.tipText}>Read our location tips</Text>
          </View>
        )
      default:
        return null
    }
  }

  return (
    <View style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity style={styles.closeButton}>
          <Text style={styles.closeButtonText}>×</Text>
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Re-up an item</Text>
        <TouchableOpacity style={styles.saveButton}>
          <Text style={styles.saveButtonText}>📁</Text>
        </TouchableOpacity>
      </View>

      {/* TODO: modify the fill ins for something more descriptive */}
      <ScrollView style={styles.content} contentContainerStyle={styles.contentContainer}>
        {/* Add Images */}
        {/* TODO: i want a maximum of 10 images and for it to be scroll-able */}
        <View style={styles.imageSection}>
          <ScrollView 
            horizontal={true}
            showsHorizontalScrollIndicator={false}
            style={styles.imageScrollView}
            contentContainerStyle={styles.imageGrid}
          >
            {[1,2,3,4,5,6,7,8,9,10].map((_, index) => (
              <TouchableOpacity 
                key={index} 
                style={styles.imageBox}
                onPress={() => handleUploadingImages(index)}
              >
                {images[index] ? (
                  <Image 
                    source={{ uri: images[index] }} 
                    style={styles.uploadedImage}
                  />
                ) : (
                  <Text style={styles.plusSign}>+</Text>
                )}
              </TouchableOpacity>
            ))}
          </ScrollView>
          <Text style={styles.addImagesText}>Add up to 10 images</Text>
        </View>

        {/* TODO: perfect length for description */}
        {/* Description */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>DESCRIPTION</Text>
          <TextInput
            style={styles.descriptionInput}
            placeholder="e.g..."
            multiline
            numberOfLines={4}
            value={description}
            onChangeText={setDescription}
          />
        </View>

        {/* Info Section */}
        <Text style={styles.sectionTitle}>INFO</Text>
        
        {/* Title */}
        <View style={styles.fieldContainer}>
          <Text style={styles.label}>Title</Text>
          <TextInput
            style={styles.input}
            placeholder="..."
            value={title}
            onChangeText={setTitle}
          />
        </View>


        {/* Category */}
        <View style={styles.fieldContainer}>
          <Text style={styles.label}>CATEGORY</Text>
          <TouchableOpacity 
            style={styles.dropdown}
            onPress={() => setShowCategoryDropdown(!showCategoryDropdown)}
          >
            <Text style={styles.dropdownText}>{category || 'Select'}</Text>
            <Text style={styles.dropdownArrow}>▼</Text>
          </TouchableOpacity>
          {showCategoryDropdown && (
            <View style={styles.dropdownOptions}>
              {categories.map(cat => (
                <TouchableOpacity 
                  key={cat}
                  style={styles.dropdownOption}
                  onPress={() => {
                    setCategory(cat)
                    setShowCategoryDropdown(false)
                  }}
                >
                  <Text style={styles.dropdownOptionText}>{cat}</Text>
                </TouchableOpacity>
              ))}
            </View>
          )}
        </View>

        {/* Brand */}
        <View style={styles.fieldContainer}>
          <Text style={styles.label}>BRAND</Text>
          <TextInput
            style={styles.input}
            placeholder="..."
            value={brand}
            onChangeText={setBrand}
          />
        </View>

        {/* Condition */}
        <View style={styles.fieldContainer}>
          <Text style={styles.label}>CONDITION</Text>
          <TouchableOpacity 
            style={styles.dropdown}
            onPress={() => setShowConditionDropdown(!showConditionDropdown)}
          >
            <Text style={styles.dropdownText}>{condition || 'Select'}</Text>
            <Text style={styles.dropdownArrow}>▼</Text>
          </TouchableOpacity>
          {showConditionDropdown && (
            <View style={styles.dropdownOptions}>
              {conditions.map(cond => (
                <TouchableOpacity 
                  key={cond}
                  style={styles.dropdownOption}
                  onPress={() => {
                    setCondition(cond)
                    setShowConditionDropdown(false)
                  }}
                >
                  <Text style={styles.dropdownOptionText}>{cond}</Text>
                </TouchableOpacity>
              ))}
            </View>
          )}
        </View>
          

        {/* TODO: exchange scroll bar is blocked by the navbar */}
        {/* Exchange */}
        <View style={styles.fieldContainer}>
          <Text style={styles.label}>EXCHANGE</Text>
          <TouchableOpacity 
            style={styles.dropdown}
            onPress={() => setShowExchangeDropdown(!showExchangeDropdown)}
          >
            <Text style={styles.dropdownText}>{listingType || 'Select'}</Text>
            <Text style={styles.dropdownArrow}>▼</Text>
          </TouchableOpacity>
          {showExchangeDropdown && (
            <View style={styles.dropdownOptions}>
              {exchangeTypes.map(type => (
                <TouchableOpacity 
                  key={type}
                  style={styles.dropdownOption}
                  onPress={() => {
                    setListingType(type)
                    setShowExchangeDropdown(false)
                  }}
                >
                  <Text style={styles.dropdownOptionText}>{type}</Text>
                </TouchableOpacity>
              ))}
            </View>
          )}
        </View>

        {/* Exchange-specific fields */}
        {renderExchangeSpecificFields()}

        {/* Pick-up Location (for non-giveaway items) */}
        {listingType && listingType !== 'Giveaway' && (
          <View style={styles.fieldContainer}>
            <Text style={styles.label}>PICK-UP LOCATION</Text>
            <TextInput
              style={styles.input}
              placeholder="..."
              value={pickUpLocation}
              onChangeText={setPickUpLocation}
            />
            {/* TODO: weird spacing between pickup date and location */}
            <Text style={styles.tipText}>Read our location tips</Text>
          </View>
        )}

        {/* Action Buttons */}
        <View style={styles.buttonContainer}>
          <TouchableOpacity style={styles.draftButton} onPress={handleSaveToDrafts}>
            <Text style={styles.draftButtonText}>SAVE TO DRAFTS</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.postButton} onPress={handlePostListing}>
            <Text style={styles.postButtonText}>POST LISTING</Text>
          </TouchableOpacity>
        </View>
      </ScrollView>
    </View>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingTop: 60,
    paddingHorizontal: 20,
    paddingBottom: 20,
    borderBottomWidth: 2,
    borderBottomColor: '#333',
  },
  closeButton: {
    padding: 5,
  },
  closeButtonText: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#333',
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
  },
  saveButton: {
    padding: 5,
  },
  saveButtonText: {
    fontSize: 20,
  },
  content: {
    flex: 1,
    paddingHorizontal: 20,
  },
  contentContainer: {
    paddingBottom: 60,
  },
  imageSection: {
  marginVertical: 20,
  },
  imageScrollView: {
    marginBottom: 10,
  },
  imageGrid: {
    flexDirection: 'row',
    paddingHorizontal: 5, // Add some padding on the sides
  },
  imageBox: {
    width: 60,
    height: 60,
    borderWidth: 2,
    borderColor: '#333',
    justifyContent: 'center',
    alignItems: 'center',
    borderRadius: 8,
    marginRight: 10, // Space between boxes
  },
  plusSign: {
    fontSize: 24,
    color: '#333',
    fontWeight: 'bold',
  },
  uploadedImage: {
    width: '100%',
    height: '100%',
    borderRadius: 8, // Match your imageBox border radius if you have one
  },
  addImagesText: {
    fontSize: 16,
    color: '#333',
    marginTop: 5,
  },
  section: {
    marginVertical: 20,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 10,
  },
  descriptionInput: {
    borderWidth: 2,
    borderColor: '#333',
    borderRadius: 8,
    padding: 15,
    fontSize: 16,
    textAlignVertical: 'top',
    minHeight: 100,
  },
  fieldContainer: {
    marginBottom: 20,
    position: 'relative',
  },
  label: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 8,
  },
  input: {
    borderWidth: 2,
    borderColor: '#333',
    borderRadius: 8,
    padding: 15,
    fontSize: 16,
  },
  dropdown: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderWidth: 2,
    borderColor: '#333',
    borderRadius: 8,
    padding: 15,
  },
  dropdownText: {
    fontSize: 16,
    color: '#666',
  },
  dropdownArrow: {
    fontSize: 12,
    color: '#333',
  },
  dropdownOptions: {
    position: 'absolute',
    top: '100%',
    left: 0,
    right: 0,
    backgroundColor: '#FFFFFF',
    borderWidth: 2,
    borderColor: '#333',
    borderRadius: 8,
    zIndex: 1000,
    marginTop: 2,
  },
  dropdownOption: {
    padding: 15,
    borderBottomWidth: 1,
    borderBottomColor: '#E0E0E0',
  },
  dropdownOptionText: {
    fontSize: 16,
    color: '#333',
  },
  exchangeField: {
    marginBottom: 20,
  },
  priceContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 2,
    borderColor: '#333',
    borderRadius: 8,
    paddingHorizontal: 15,
  },
  dollarSign: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
    marginRight: 5,
  },
  priceInput: {
    flex: 1,
    fontSize: 16,
    paddingVertical: 15,
  },
  tipText: {
    fontSize: 12,
    color: '#999',
    marginTop: 5,
    textDecorationLine: 'underline',
  },
  buttonContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginVertical: 30,
  },
  draftButton: {
    flex: 1,
    borderWidth: 2,
    borderColor: '#333',
    borderRadius: 8,
    padding: 15,
    alignItems: 'center',
    marginRight: 10,
  },
  draftButtonText: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#333',
  },
  postButton: {
    flex: 1,
    backgroundColor: '#333',
    borderRadius: 8,
    padding: 15,
    alignItems: 'center',
    marginLeft: 10,
  },
  postButtonText: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#FFFFFF',
  },
})

export default Post