import { View, Text, TextInput, FlatList, TouchableOpacity, StyleSheet, SafeAreaView } from 'react-native'
import React from 'react'

interface DirectMessage {
  id: string;
  userName: string;
  initials: string;
  lastMessage: string;
  timestamp: string;
  avatarColor: string;
  unreadCount?: number;
}

const hardcodedMessages: DirectMessage[] = [
  {
    id: '1',
    userName: 'jennadoodles',
    initials: 'JW',
    lastMessage: 'Hey! Do you still have that HDMI cable?',
    timestamp: '2m ago',
    avatarColor: '#A8D0F0',
    unreadCount: 2,
  },
  {
    id: '2',
    userName: 'kevinlearns',
    initials: 'KL',
    lastMessage: 'Thanks so much for letting me borrow the fan!',
    timestamp: '15m ago',
    avatarColor: '#F5E050',
  },
  {
    id: '3',
    userName: 'sarahstudies',
    initials: 'SM',
    lastMessage: 'Perfect! I\'ll pick it up in 10 minutes',
    timestamp: '45m ago',
    avatarColor: '#A8E6CF',
    unreadCount: 1,
  },
  {
    id: '4',
    userName: 'mikerides',
    initials: 'MR',
    lastMessage: 'You\'re a lifesaver! Car started right up 🚗',
    timestamp: '1h ago',
    avatarColor: '#FFB3B3',
  },
  {
    id: '5',
    userName: 'alexcodes',
    initials: 'AC',
    lastMessage: 'Want to grab coffee and study together?',
    timestamp: '2h ago',
    avatarColor: '#D4B3FF',
    unreadCount: 3,
  },
  {
    id: '6',
    userName: 'emmaarts',
    initials: 'EA',
    lastMessage: 'The projector worked perfectly for my presentation!',
    timestamp: '3h ago',
    avatarColor: '#FFD4B3',
  },
  {
    id: '7',
    userName: 'tomfixes',
    initials: 'TF',
    lastMessage: 'I have a spare ethernet cable if you need it',
    timestamp: '5h ago',
    avatarColor: '#C8E6C9',
  },
  {
    id: '8',
    userName: 'lisareads',
    initials: 'LR',
    lastMessage: 'Thanks for the textbook recommendation!',
    timestamp: '1d ago',
    avatarColor: '#FFE0B2',
  },
];

const community = () => {
  const renderMessageItem = ({ item }: { item: DirectMessage }) => (
    <TouchableOpacity style={styles.messageItem}>
      <View style={[styles.avatar, { backgroundColor: item.avatarColor }]}>
        <Text style={styles.initials}>{item.initials}</Text>
      </View>
      
      <View style={styles.messageContent}>
        <View style={styles.messageHeader}>
          <Text style={styles.userName}>{item.userName}</Text>
          <Text style={styles.timestamp}>{item.timestamp}</Text>
        </View>
        <Text style={styles.lastMessage} numberOfLines={1}>
          {item.lastMessage}
        </Text>
      </View>
      
      {item.unreadCount && (
        <View style={styles.unreadBadge}>
          <Text style={styles.unreadText}>{item.unreadCount}</Text>
        </View>
      )}
    </TouchableOpacity>
  );

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.searchContainer}>
        <TextInput
          style={styles.searchInput}
          placeholder="Search messages..."
          placeholderTextColor="#999"
        />
      </View>
      
      <FlatList
        data={hardcodedMessages}
        keyExtractor={(item) => item.id}
        renderItem={renderMessageItem}
        style={styles.messagesList}
        showsVerticalScrollIndicator={false}
        contentContainerStyle={styles.messagesContainer}
      />
    </SafeAreaView>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F8F9FA',
  },
  searchContainer: {
    paddingHorizontal: 16,
    paddingTop: 16,
    paddingBottom: 8,
  },
  searchInput: {
    height: 44,
    backgroundColor: 'white',
    borderRadius: 12,
    paddingHorizontal: 16,
    fontSize: 16,
    borderWidth: 1,
    borderColor: '#E1E5E9',
  },
  postsList: {
    flex: 1,
  },
  messagesContainer: {
    paddingBottom: 100, // Extra padding for nav bar
  },
  messagesList: {
    flex: 1,
  },
  messageItem: {
    flexDirection: 'row',
    padding: 16,
    backgroundColor: 'white',
    borderBottomWidth: 1,
    borderBottomColor: '#F0F0F0',
    alignItems: 'center',
  },
  avatar: {
    width: 48,
    height: 48,
    borderRadius: 24,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  initials: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
  },
  postContent: {
    flex: 1,
  },
  messageContent: {
    flex: 1,
    marginRight: 8,
  },
  postHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 8,
  },
  messageHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 4,
  },
  userName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#333',
  },
  location: {
    fontSize: 14,
    color: '#666',
    marginTop: 2,
  },
  timestamp: {
    fontSize: 14,
    color: '#999',
  },
  message: {
    fontSize: 15,
    color: '#333',
    lineHeight: 20,
    marginBottom: 12,
  },
  lastMessage: {
    fontSize: 15,
    color: '#666',
    lineHeight: 20,
  },
  respondButton: {
    borderWidth: 1,
    borderColor: '#E1E5E9',
    borderRadius: 20,
    paddingHorizontal: 16,
    paddingVertical: 8,
    alignSelf: 'flex-start',
  },
  respondText: {
    fontSize: 14,
    color: '#666',
    fontWeight: '500',
  },
  unreadBadge: {
    backgroundColor: '#007AFF',
    borderRadius: 10,
    minWidth: 20,
    height: 20,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: 6,
  },
  unreadText: {
    color: 'white',
    fontSize: 12,
    fontWeight: '600',
  },
});

export default community