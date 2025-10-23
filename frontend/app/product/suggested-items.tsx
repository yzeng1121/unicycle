import { View, Text, ScrollView, TouchableOpacity, StyleSheet, Image } from 'react-native'
import React from 'react'
import { Ionicons } from '@expo/vector-icons'
import { useRouter } from 'expo-router'
import { data, getItemImage } from "../data/listings" // Adjust path as needed

type Item = {
  id: string;        
  listingType: 'Selling' | 'Giveaway' | 'Trading' | 'Lending';
  category: string;
  size?: string;
  condition: string;
  price: number;
  rate?: string;
};

const ProductGrid = () => {
  const router = useRouter()

  const ProductCard = ({ item }) => (
    <TouchableOpacity 
      style={styles.productCard}
      onPress={() => router.push(`./${item.id}`)}
    >
      <View style={styles.imageContainer}>
        <Image
          style={styles.productImage}
          source={getItemImage(item, 0)}
        />
        <TouchableOpacity style={styles.bookmarkButton}>
          <Ionicons name="bookmark-outline" size={16} color="#000" />
        </TouchableOpacity>
      </View>
      <Text style={styles.categoryText}>{item.category}</Text>
      {item.listingType === 'Selling' && (
        <Text style={styles.priceText}>${item.price.toFixed(2)}</Text>
      )}
      {item.listingType === 'Giveaway' && (
        <Text style={styles.priceText}>Free</Text>
      )}
      {item.listingType === 'Trading' && (
        <Text style={styles.priceText}>Trading</Text>
      )}
      {item.listingType === 'Lending' && (
        <Text style={styles.priceText}>${item.price.toFixed(2)} {item.rate}</Text>
      )}
    </TouchableOpacity>
  )

  return (
    <View style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => router.back()} style={styles.backButton}>
          <Ionicons name="chevron-back" size={24} color="#000" />
        </TouchableOpacity>
        
        <TouchableOpacity style={styles.basketButton}>
          <Ionicons name="basket-outline" size={24} color="#000" />
        </TouchableOpacity>
      </View>

      {/* TODO: make it so that "SUGGESTED" is not another header (clutter) */}
      {/* Title and Sort Section */}
      <View style={styles.titleSection}>
        <Text style={styles.headerTitle}>SUGGESTED</Text>
        <TouchableOpacity style={styles.sortButton}>
          <Ionicons name="swap-vertical" size={20} color="#000" />
          <Text style={styles.sortText}>Sort by</Text>
        </TouchableOpacity>
      </View>

      {/* Product Grid */}
      <ScrollView style={styles.scrollView} contentContainerStyle={styles.gridContainer}>
        <View style={styles.grid}>
          {data.map((item) => (
            <ProductCard key={item.id} item={item} />
          ))}
        </View>
      </ScrollView>
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
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingTop: 50,
    paddingBottom: 15,
    borderBottomWidth: 1,
    borderBottomColor: '#eee',
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
})

export default ProductGrid