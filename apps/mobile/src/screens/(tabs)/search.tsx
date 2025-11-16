import { View, Text, TextInput, TouchableOpacity, ScrollView, StyleSheet } from 'react-native'
import { useState } from 'react'

import { useAuth } from '../../contexts/auth-context';
import { router } from 'expo-router';

const Search = () => {
  const [searchText, setSearchText] = useState('');
  const { makeAuthenticatedRequest, accessToken, user } = useAuth();
  const [userId, setUserId] = useState('');
  
  if (!user?.userId) {
    console.error('User not authenticated or user ID not available');
    return;
  }

  // TODO: cache recent searches
  const recentSearches = [
    'tickets',
    'textbooks',
    'mini fridge',
    'desk lamp'
  ]

  const clearSearch = () => {
    setSearchText('')
  }

  const handleRecentSearch = async (searchTerm: string) => {
    setSearchText(searchTerm)
  }

  const searchForItem = async (): Promise<void> => {
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
    <View style={styles.container}>
      {/* Search Bar */}
      <View style={styles.searchContainer}>
        <View style={styles.searchBar}>
          <TextInput
            style={styles.searchInput}
            placeholder="Search items, categories..."
            placeholderTextColor="#999"
            value={searchText}
            onChangeText={setSearchText}
            onSubmitEditing={searchForItem}     
          />
          {searchText.length > 0 && (
            <TouchableOpacity onPress={clearSearch} style={styles.clearButton}>
              <Text style={styles.clearButtonText}>×</Text>
            </TouchableOpacity>
          )}
        </View>
      </View>

      {/* Recent Searches */}
      <ScrollView style={styles.content}>
        <View style={styles.recentSearchesContainer}>
          <View style={styles.recentSearchesHeader}>
            <Text style={styles.recentSearchesTitle}>Recent Searches</Text>
          </View>
          
          <View style={styles.recentSearchesList}>
            {recentSearches.map((search, index) => (
              <TouchableOpacity 
                key={index} 
                style={styles.recentSearchItem}
                onPress={() => handleRecentSearch(search)}
              >
                <Text style={styles.recentSearchText}>{search}</Text>
                <Text style={styles.arrowText}></Text>
              </TouchableOpacity>
            ))}
          </View>
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
  searchContainer: {
    paddingTop: 60,
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
  clearButton: {
    padding: 5,
  },
  clearButtonText: {
    fontSize: 24,
    color: '#666',
    fontWeight: 'bold',
  },
  content: {
    flex: 1,
    paddingHorizontal: 20,
  },
  recentSearchesContainer: {
    marginTop: 20,
  },
  recentSearchesHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 15,
  },
  recentSearchesTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
  },
  seeAllText: {
    fontSize: 14,
    color: '#666',
  },
  recentSearchesList: {
    marginTop: 10,
  },
  recentSearchItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 15,
    borderBottomWidth: 1,
    borderBottomColor: '#E0E0E0',
  },
  recentSearchText: {
    fontSize: 16,
    color: '#333',
  },
  arrowText: {
    fontSize: 18,
    color: '#666',
    fontWeight: 'bold',
  },
  bottomNav: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    alignItems: 'center',
    paddingVertical: 15,
    paddingHorizontal: 20,
    borderTopWidth: 2,
    borderTopColor: '#333',
    backgroundColor: '#FFFFFF',
  },
  navItem: {
    alignItems: 'center',
  },
  navIcon: {
    width: 40,
    height: 40,
    borderRadius: 20,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F0F0F0',
  },
  navIconText: {
    fontSize: 18,
  },
  addButton: {
    backgroundColor: '#333',
    width: 50,
    height: 50,
    borderRadius: 25,
  },
  addButtonText: {
    fontSize: 24,
    color: '#FFFFFF',
    fontWeight: 'bold',
  },
})

export default Search