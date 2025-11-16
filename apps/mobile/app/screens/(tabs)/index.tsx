import { View, Text, Image, TextInput, ScrollView, TouchableOpacity, StyleSheet } from 'react-native'
import React, { useState } from 'react'
import { useRouter } from "expo-router";

import { data, getItemImage } from "../../sample_data/listings";
import { requests } from "../../sample_data/asks";
import { searchForItem } from "./search";

// TODO: create more centralized Item & QuickAsk type to avoid errors
type Item = {
  id: string;        
  userId: string;
  itemName: string;  
  listingType: 'Selling' | 'Giveaway' | 'Trading' | 'Lending';
  category: string;
  subcategory: string;
  size?: string;
  condition: string;
  price: number;
  rate?: string;
};

type User = {
  id: string,
  name: string,
  profileImage: string
}

type QuickAsk = {
  id: number;
  username: string;
  initials: string;
  message: string;
  time: string;
  bgColor: string;
};

const Home = () => {
  const router = useRouter();

  const ItemCard = ({ item }: { item: Item }) => (
    <TouchableOpacity 
      style={[styles.itemCard]}
      onPress={() => router.push(`../product/${item.id}`)}>

      <View style={styles.itemContent}>
        <Image
          style={styles.imageContainer}
          source={getItemImage(item, 0)}
        />
        {item.listingType === 'Selling' && (
          <Text style={styles.itemPrice}>${item.price.toFixed(2)} - Selling</Text>
        )}
        {item.listingType === 'Giveaway' && (
          <Text style={styles.itemPrice}>Free - Giveaway</Text>
        )}
        {item.listingType === 'Trading' && (
          <Text style={styles.itemPrice}>Trading</Text>
        )}
        {item.listingType === 'Lending' && (
          <Text style={styles.itemPrice}>${item.price.toFixed(2)} {item.rate}</Text>
        )}
      </View>
    </TouchableOpacity>
  )

  const QuickAskCard = ({ ask }: { ask: QuickAsk }) => (
    <View style={styles.quickAskCard}>
      <View style={[styles.avatar, { backgroundColor: ask.bgColor }]}>
        <Text style={styles.avatarText}>{ask.initials}</Text>
      </View>
      <View style={styles.quickAskContent}>
        <View style={styles.quickAskHeader}>
          <Text style={styles.quickAskName}>{ask.username}</Text>
          <Text style={styles.quickAskTime}>{ask.time}</Text>
        </View>
        <Text style={styles.quickAskMessage}>{ask.message}</Text>
        <TouchableOpacity 
          style={styles.respondButton}
          onPress={() => router.push(`../../user/${ask.id}`)}
        >
          <Text style={styles.respondButtonText}>Respond</Text>
        </TouchableOpacity>
      </View>
    </View>
  )
  
  const [searchText, setSearchText] = useState('')

  const clearSearch = () => {
    setSearchText('')
  }

  const handleSearch = () => {
    const query = searchText.trim();
      console.log("Query is " + query);
      if (!query) return;
      router.push({
        pathname: `../product/search-items`,
        params: {
          query: query
        }
      });
  }

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
      {/* Search Bar */}
      <View style={styles.searchContainer}>
        <View style={styles.searchBar}>
          <TextInput
            style={styles.searchInput}
            placeholder="Search items, categories..."
            placeholderTextColor="#999"
            value={searchText}
            onChangeText={setSearchText}
            onSubmitEditing={handleSearch}  
          />
          {/* TODO: make search actually work here */}
        </View>
      </View>

      {/* Suggested Items Section */}
      <View style={styles.section}>
        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>Suggested</Text>
          <TouchableOpacity>
            <Text 
              style={styles.seeAllButton}
              onPress={() => router.push(`../product/suggested-items`)}
            >See All</Text>
          </TouchableOpacity>
        </View>
        {/* This adds test data from the Listings file into the actual screen.*/}
        <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.horizontalScroll}>
          {data.slice(0, 6).map(item => (
            <ItemCard key={item.id} item={item} />
          ))}
        </ScrollView>
      </View>

      {/* Items Near You Section */}
      <View style={styles.section}>
        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>Items Near You</Text>
          <TouchableOpacity>
            <Text 
              style={styles.seeAllButton}
              onPress={() => router.push(`../product/nearby-items`)}
            >See All</Text>
          </TouchableOpacity>
        </View>
        {/* This adds test data from the Listings file into the actual screen.*/}
        <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.horizontalScroll}>
          {data.slice(0, 6).map(item => (
            <ItemCard key={item.id} item={item} />
          ))}
        </ScrollView>
      </View>

      {/* Quick Asks Section */}
      <View style={styles.section}>
        <View style={styles.sectionHeader}>
          <Text style={styles.sectionTitle}>Quick Asks</Text>
          <TouchableOpacity>
            <Text 
              style={styles.seeAllButton}
              onPress={() => router.push(`../asks/quick-asks`)}
            >See All</Text>
          </TouchableOpacity>
        </View>

        <View style={styles.quickAsksContainer}>
          {requests.slice(0, 2).map(ask => (
            <QuickAskCard key={ask.id} ask={ask} />
          ))}
        </View>
      </View>
    </ScrollView>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F8F9FA',
    paddingTop: 60,
  },
  contentContainer: {
    paddingBottom: 60,
  },
  searchContainer: {
    paddingHorizontal: 20,
    paddingBottom: 20,
    borderBottomWidth: 1,
    borderBottomColor: '#E0E0E0',
  },
  searchBar: {
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 2,
    borderColor: '#333',
    borderRadius: 8,
    paddingHorizontal: 15,
    backgroundColor: '#FFFFFF',
  },
  searchInput: {
    flex: 1,
    fontSize: 16,
    paddingVertical: 12,
    color: '#333',
  },
  filterContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 20,
    marginBottom: 30,
  },
  filterLabel: {
    fontSize: 16,
    color: '#666',
    marginRight: 15,
  },
  filterPill: {
    backgroundColor: '#A9DFBF',
    borderRadius: 20,
    paddingHorizontal: 15,
    paddingVertical: 8,
    marginRight: 10,
  },
  filterText: {
    color: '#333',
    fontSize: 14,
    fontWeight: '500',
  },
  section: {
    marginBottom: 30,
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 15,
    paddingHorizontal: 20,
    marginBottom: 15,
  },
  sectionTitle: {
    fontSize: 22,
    fontWeight: 'bold',
    color: '#333',
  },
  seeAllButton: {
    fontSize: 16,
    color: '#A9DFBF',
    fontWeight: '500',
  },
  horizontalScroll: {
    paddingLeft: 10,
  },
  itemCard: {
    width: 180,
    height: 250,
    borderRadius: 12,
    marginRight: 10,
    padding: 15,
    justifyContent: 'flex-end',

    backgroundColor: 'white',

    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.1,
    shadowRadius: 3.84,
    elevation: 5,
  },
  imageContainer: {
    width: '100%',
    height: 190,
    backgroundColor: '#f5f5f5',
    borderRadius: 8,
    marginTop: 8,
    marginBottom: 8,
    overflow: 'hidden',
    position: 'relative',
  },
  itemContent: {
    flex: 1,
    justifyContent: 'flex-end',
  },
  itemTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 5,
  },
  itemPrice: {
    fontSize: 16,
    color: '#666',
    marginBottom: 2,
  },
  itemSubtitle: {
    fontSize: 12,
    color: '#666',
    marginBottom: 2,
  },
  itemLocation: {
    fontSize: 14,
    color: '#888',
  },
  quickAsksContainer: {
    paddingHorizontal: 20,
  },
  quickAskCard: {
    flexDirection: 'row',
    backgroundColor: 'white',
    borderRadius: 12,
    padding: 15,
    marginBottom: 15,
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 2,
    },
    shadowOpacity: 0.1,
    shadowRadius: 3.84,
    elevation: 5,
  },
  avatar: {
    width: 50,
    height: 50,
    borderRadius: 25,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 15,
  },
  avatarText: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
  },
  quickAskContent: {
    flex: 1,
  },
  quickAskHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 5,
  },
  quickAskName: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
  },
  quickAskTime: {
    fontSize: 12,
    color: '#999',
  },
  quickAskMessage: {
    fontSize: 14,
    color: '#666',
    marginBottom: 10,
    lineHeight: 20,
  },
  respondButton: {
    alignSelf: 'flex-start',
    paddingHorizontal: 15,
    paddingVertical: 6,
    borderRadius: 15,
    borderWidth: 1,
    borderColor: '#A9DFBF',
  },
  respondButtonText: {
    fontSize: 12,
    color: '#A9DFBF',
    fontWeight: '500',
  },
})

export default Home