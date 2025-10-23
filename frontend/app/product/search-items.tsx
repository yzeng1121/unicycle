import { View, Text, ScrollView, TouchableOpacity, StyleSheet, Image, TextInput } from 'react-native'
import React, { useState, useEffect } from 'react'
import { Ionicons } from '@expo/vector-icons'
import { useRouter } from 'expo-router'
import { useLocalSearchParams } from 'expo-router';
import { useAuth } from '../contexts/auth-context';

type Item = {
  id: string;     
  userId: string;   
  listingType: 'Selling' | 'Giveaway' | 'Trading' | 'Lending';
  category: string;
  size?: string;
  condition: string;
  price: number;
  rate?: string;
};

// TODO: add number of results + search bar at top
const ProductGrid = () => {
  const { makeAuthenticatedRequest, user } = useAuth();
  const [searchText, setSearchText] = useState('');
  const [listings, setListings] = useState<any[]>([]);
  const { query } = useLocalSearchParams();
  const router = useRouter()

  const NoResultsScreen: React.FC = () => (
    <View style={styles.container}>
      <Text style={styles.shrug}>¯\_(ツ)_/¯</Text>
      <Text style={styles.message}>Sorry! No results found.</Text>
      <Text style={styles.subMessage}>Try searching for other items.</Text>
    </View>
  );

  console.log("Query is now... " + query);

  const clearSearch = () => {
    setSearchText('')
  }

  useEffect(() => {
    if (query && user?.userId) {
      getListings();
    }
  }, [query, user?.userId]);

  const handleNewSearch = async () => {
    if (searchText.trim()) {
      // Update the internal state directly
      const newQuery = searchText.trim();
      
      console.log("The id of the user who's prompting a search is..." + user.userId);
      try {
        const response = await makeAuthenticatedRequest(
          `http://10.243.71.82:8080/api/listings/search?userId=${user.userId}&query=${encodeURIComponent(newQuery)}`
        );

        if (response.ok) {
          const results = await response.json();
          setListings(results || []);
        }
      } catch (error) {
        console.error('Search error:', error);
        setListings([]);
      }
    }
  };

  const getListings = async () => {
     // TODO: query the database looking for specific keywords
      // brand, title are main keywords to search for
    try {
      // const response = await makeAuthenticatedRequest('http://10.243.71.82:8080/api/listings/create', {
      // const response = await makeAuthenticatedRequest('http://192.168.86.46:8080/api/listings/create', {
      console.log("UserId is " + user.userId);
      const response = await makeAuthenticatedRequest(
        `http://10.243.71.82:8080/api/listings/search?userId=${user.userId}&query=${encodeURIComponent(query)}`, {

      });

      if (response.ok) {
        const results = await response.json();
        console.log("Search results: " + results);
        console.log("Search results (as string): " + JSON.stringify(results));

        // ✅ Just set the listings, don't return JSX here
        setListings(results || []);
      } else {
        console.error('Search failed:', response.status);
        setListings([]);
      }
      
    } catch (error) {
      console.error('Search error:', error);
    }
  }

  const ProductCard = ({ listing }) => (
    <TouchableOpacity 
      style={styles.productCard}
      onPress={() => router.push(`./${listing.itemId}`)}
    >
      <View style={styles.imageContainer}>
        <Image
          style={styles.productImage}
          source={{ uri: listing.coverImage }}
        />
        <TouchableOpacity style={styles.bookmarkButton}>
          <Ionicons name="bookmark-outline" size={16} color="#000" />
        </TouchableOpacity>
      </View>
      <Text style={styles.categoryText}>{listing.category}</Text>
      {listing.listingType === 'Selling' && (
        <Text style={styles.priceText}>${listing.price.toFixed(2)}</Text>
      )}
      {listing.listingType === 'Giveaway' && (
        <Text style={styles.priceText}>Free</Text>
      )}
      {listing.listingType === 'Trading' && (
        <Text style={styles.priceText}>Trading</Text>
      )}
      {listing.listingType === 'Lending' && (
        <Text style={styles.priceText}>${listing.price.toFixed(2)} {listing.rate}</Text>
      )}
    </TouchableOpacity>
  )

  return (
    <View style={styles.container}>
      {/* Header with Search Bar */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => router.back()} style={styles.backButton}>
          <Ionicons name="chevron-back" size={24} color="#000" />
        </TouchableOpacity>
        
        {/* Search Bar */}
        <View style={styles.searchBar}>
          <TextInput
            style={styles.searchInput}
            placeholder={query}
            placeholderTextColor="#333"
            value={searchText}
            onChangeText={setSearchText}
            onSubmitEditing={handleNewSearch}     
          />
          {searchText.length > 0 && (
            <TouchableOpacity onPress={clearSearch} style={styles.clearButton}>
              <Text style={styles.clearButtonText}>×</Text>
            </TouchableOpacity>
          )}
        </View>

        <TouchableOpacity style={styles.basketButton}>
          <Ionicons name="basket-outline" size={24} color="#000" />
        </TouchableOpacity>
      </View>

      {/* Title and Sort Section */}
      <View style={styles.titleSection}>
        <TouchableOpacity style={styles.sortButton}>
          <Ionicons name="swap-vertical" size={20} color="#000" />
          <Text style={styles.sortText}>Sort by</Text>
        </TouchableOpacity>
      </View>

      {/* Product Grid */}
      {listings.length === 0 ? (
        <NoResultsScreen />
      ) : (
        <ScrollView style={styles.scrollView} contentContainerStyle={styles.gridContainer}>
          <View style={styles.grid}>
            {listings.map((listing) => (
              <ProductCard key={listing.itemId} listing={listing} />
            ))}
          </View>
        </ScrollView>
      )}
    </View>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 20,
    paddingTop: 60,
    paddingBottom: 20,
    borderBottomWidth: 1,
    borderBottomColor: '#E0E0E0',
    backgroundColor: '#fff',
    gap: 15,
  },
  searchBar: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 2,
    borderColor: '#333',
    borderRadius: 8,
    paddingHorizontal: 15,
    backgroundColor: '#FFFFFF',
    height: 44,
  },
  searchInput: {
    flex: 1,
    fontSize: 16,
    paddingVertical: 8,
    color: '#333',
  },
  clearButton: {
    padding: 5,
  },
  clearButtonText: {
    fontSize: 24,
    color: '#666',
    fontWeight: 'bold',
  },
  titleSection: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 15,
  },
  backButton: {
    padding: 5,
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#000',
    letterSpacing: 1,
  },
  headerRight: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 15,
  },
  sortButton: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#f0f0f0',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 15,
    gap: 5,
    marginLeft: 'auto'
  },
  sortText: {
    fontSize: 12,
    color: '#000',
  },
  basketButton: {
    padding: 5,
  },
  scrollView: {
    flex: 1,
  },
  gridContainer: {
    padding: 20,
  },
  grid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
  },
  productCard: {
    width: '48%',
    marginBottom: 20,
    backgroundColor: '#fff',
  },
  imageContainer: {
    width: '100%',
    height: 220,
    backgroundColor: '#f5f5f5',
    borderRadius: 8,
    marginBottom: 8,
    overflow: 'hidden',
    position: 'relative',
  },
  productImage: {
    width: '100%',
    height: '100%',
    resizeMode: 'cover',
  },
  bookmarkButton: {
    position: 'absolute',
    top: 8,
    right: 8,
    backgroundColor: 'rgba(255, 255, 255, 0.9)',
    borderRadius: 12,
    padding: 4,
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 1,
    },
    shadowOpacity: 0.2,
    shadowRadius: 2,
    elevation: 2,
  },
  categoryText: {
    fontSize: 12,
    color: '#000',
    fontWeight: '500',
    marginBottom: 4,
  },
  priceText: {
    fontSize: 14,
    color: '#ff6b6b',
    fontWeight: 'bold',
  },
  noResultsContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 20,
  },
  shrug: {
    fontSize: 48,
    color: '#999',
    marginBottom: 16,
    textAlign: 'center',
    marginTop: 150
  },
  message: {
    fontSize: 18,
    color: '#666',
    fontWeight: '500',
    marginBottom: 8,
    textAlign: 'center',
  },
  subMessage: {
    fontSize: 14,
    color: '#999',
    textAlign: 'center',
  },
})

export default ProductGrid