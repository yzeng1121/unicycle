import { View, Text, ScrollView, TouchableOpacity, StyleSheet, TextInput } from 'react-native'
import React from 'react'
import { useRouter } from 'expo-router'
import { Ionicons } from '@expo/vector-icons'
import { requests } from "../data/asks" // Adjust path as needed

// TODO: each user can only have 1 ask to reduce data
type Ask = {
  id: string | number
  bgColor: string
  initials: string
  username: string
  location: string
  time: string
  message: string
}

const QuickAsks = () => {
  const router = useRouter()

  const QuickAskCard = ({ ask }: { ask: Ask }) => (
    <View style={styles.askCard}>
      <View style={styles.askHeader}>
        <View style={styles.userInfo}>
          <View style={[styles.avatar, { backgroundColor: ask.bgColor }]}>
            <Text style={styles.avatarText}>{ask.initials}</Text>
          </View>
          <View style={styles.userDetails}>
            <Text style={styles.userName}>{ask.username}</Text>
            <Text style={styles.userLocation}>{ask.location}</Text>
          </View>
        </View>
        <Text style={styles.timeStamp}>{ask.time}</Text>
      </View>
      
      <Text style={styles.askMessage}>{ask.message}</Text>
      
      <TouchableOpacity 
        style={styles.respondButton}
        onPress={() => router.push(`../../user/${ask.id}`)}>
        <Text style={styles.respondButtonText}>Respond</Text>
      </TouchableOpacity>
    </View>
  )

  return (
    <View style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={() => router.back()} style={styles.backButton}>
          <Ionicons name="chevron-back" size={24} color="#000" />
        </TouchableOpacity>
        
        <TouchableOpacity style={styles.addButton}>
          <Ionicons name="add" size={24} color="#000" />
        </TouchableOpacity>
      </View>

      {/* TODO: remove the header bc quick asks title shouldnt be covering the feed */}
      {/* Title and Sort Section */}
      <View style={styles.titleSection}>
        <Text style={styles.headerTitle}>QUICK ASKS</Text>
        <TouchableOpacity style={styles.sortButton}>
          <Ionicons name="swap-vertical" size={20} color="#000" />
          {/* TODO: implement sort by/filtering feature */}
          <Text style={styles.sortText}>Sort by</Text>
        </TouchableOpacity>
      </View>

      {/* Quick Asks List */}
      <ScrollView style={styles.scrollView} contentContainerStyle={styles.contentContainer}>
        {requests.map(ask => (
          <QuickAskCard key={ask.id} ask={ask} />
        ))}
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
  addButton: {
    padding: 5,
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#000',
    letterSpacing: 1,
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
  scrollView: {
    flex: 1,
  },
  contentContainer: {
    paddingHorizontal: 20,
    paddingBottom: 20,
  },
  askCard: {
    backgroundColor: '#fff',
    borderRadius: 0,
    paddingVertical: 20,
    borderBottomWidth: 1,
    borderBottomColor: '#eee',
  },
  askHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 12,
  },
  userInfo: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
  },
  avatar: {
    width: 40,
    height: 40,
    borderRadius: 20,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  avatarText: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#333',
  },
  userDetails: {
    flex: 1,
  },
  userName: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#000',
    marginBottom: 2,
  },
  userLocation: {
    fontSize: 12,
    color: '#666',
  },
  timeStamp: {
    fontSize: 12,
    color: '#999',
    marginLeft: 10,
  },
  askMessage: {
    fontSize: 14,
    color: '#000',
    marginBottom: 15,
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

export default QuickAsks