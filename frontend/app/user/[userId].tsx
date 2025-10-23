import React, { useState, useRef, useEffect } from 'react';
import { 
  View, 
  Text, 
  ScrollView, 
  TouchableOpacity, 
  StyleSheet, 
  TextInput,
  SafeAreaView,
  Platform,
  KeyboardAvoidingView
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useRouter, useLocalSearchParams } from 'expo-router';
import { requests } from '../data/asks';

interface Message {
  id: string;
  text: string;
  timestamp: Date;
  isFromCurrentUser: boolean;
}
// TODO: make so that message stays even after exiting a group chat
// TODO: can sort of adapt a snapchat feature where messages dont save upon leaving the app

const DirectMessage: React.FC = () => {
  const router = useRouter();

  const { userId } = useLocalSearchParams();
  const user = requests.find(user => user.id === userId);

  const [messages, setMessages] = useState<Message[]>([
    {
      id: '1',
      text: 'Hey there! How are you doing?',
      timestamp: new Date(Date.now() - 60000),
      isFromCurrentUser: false,
    },
    {
      id: '2',
      text: 'I\'m doing great, thanks for asking! How about you?',
      timestamp: new Date(Date.now() - 30000),
      isFromCurrentUser: true,
    },
  ]);
  const [newMessage, setNewMessage] = useState('');
  const scrollViewRef = useRef<ScrollView>(null);

  // Auto-scroll to bottom when new messages arrive
  useEffect(() => {
    scrollViewRef.current?.scrollToEnd({ animated: true });
  }, [messages]);

  const handleSendMessage = () => {
    if (newMessage.trim() === '') return;

    const message: Message = {
      id: Date.now().toString(),
      text: newMessage.trim(),
      timestamp: new Date(),
      isFromCurrentUser: true,
    };

    setMessages(prev => [...prev, message]);
    setNewMessage('');
  };

  const goBack = () => {
    router.back();
  };

  return (
    <SafeAreaView style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity onPress={goBack} style={styles.backButton}>
          <Ionicons name="chevron-back" size={24} color="#000" />
        </TouchableOpacity>
        
        <Text style={styles.username}>
          {user ? `${user.username}` : 'User Not Found'}
        </Text>
        
        <View style={styles.profileButton}>
          {/* TODO: map user to their designated profile photo */}
          <Ionicons name="person" size={20} color="#666" />
        </View>
      </View>

      {/* TODO: add animation for keyboardDismissMode */}
      {/* Messages */}
      <ScrollView 
        ref={scrollViewRef}
        style={styles.messagesContainer}
        contentContainerStyle={styles.messagesContent}
        showsVerticalScrollIndicator={false}
        keyboardDismissMode="on-drag"  
        keyboardShouldPersistTaps="handled" 
      >
        {messages.map((message) => (
          <View
            key={message.id}
            style={[
              styles.messageWrapper,
              message.isFromCurrentUser ? styles.myMessageWrapper : styles.otherMessageWrapper
            ]}
          >
            <View
              style={[
                styles.messageBubble,
                message.isFromCurrentUser ? styles.myMessage : styles.otherMessage
              ]}
            >
              <Text style={[
                styles.messageText,
                message.isFromCurrentUser ? styles.myMessageText : styles.otherMessageText
              ]}>
                {message.text}
              </Text>
            </View>
          </View>
        ))}
      </ScrollView>

      {/* TODO: make so that when user scrolls up the inputContainer, keyboard leaves */}
      {/* Message Input */}
      <KeyboardAvoidingView 
        behavior="padding" 
        style={styles.inputContainer}
        keyboardVerticalOffset={15}
      >
        <TextInput
          style={styles.textInput}
          value={newMessage}
          onChangeText={setNewMessage}
          placeholder="Send a message..."
          placeholderTextColor="#999"
          multiline
          maxLength={500}
        />
        <TouchableOpacity 
          onPress={handleSendMessage}
          style={[
            styles.sendButton,
            newMessage.trim() === '' && styles.sendButtonDisabled
          ]}
          disabled={newMessage.trim() === ''}
        >
          <Ionicons 
            name="send" 
            size={20} 
            color={newMessage.trim() === '' ? '#ccc' : '#007AFF'} 
          />
        </TouchableOpacity>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#d1d5db',
    backgroundColor: '#fff',
  },
  backButton: {
    padding: 4,
  },
  username: {
    fontSize: 18,
    fontWeight: '600',
    color: '#000',
    flex: 1,
    textAlign: 'center',
    marginHorizontal: 16,
  },
  profileButton: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#f9fafb',
    borderWidth: 1,
    borderColor: '#9ca3af',
    alignItems: 'center',
    justifyContent: 'center',
  },
  messagesContainer: {
    flex: 1,
    backgroundColor: '#fff',
  },
  messagesContent: {
    paddingHorizontal: 16,
    paddingVertical: 24,
  },
  messageWrapper: {
    marginBottom: 16,
  },
  myMessageWrapper: {
    alignItems: 'flex-end',
  },
  otherMessageWrapper: {
    alignItems: 'flex-start',
  },
  messageBubble: {
    maxWidth: '75%',
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderRadius: 16,
  },
  myMessage: {
    backgroundColor: '#3b82f6',
    borderBottomRightRadius: 6,
  },
  otherMessage: {
    backgroundColor: '#f3f4f6',
    borderBottomLeftRadius: 6,
  },
  messageText: {
    fontSize: 14,
    lineHeight: 20,
  },
  myMessageText: {
    color: '#fff',
  },
  otherMessageText: {
    color: '#000',
  },
  inputContainer: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderTopWidth: 1,
    borderTopColor: '#d1d5db',
    backgroundColor: '#fff',
  },
  textInput: {
    flex: 1,
    borderWidth: 1,
    borderColor: '#d1d5db',
    borderRadius: 20,
    paddingHorizontal: 16,
    paddingVertical: 8,
    fontSize: 16,
    maxHeight: 120,
    minHeight: 40,
    marginRight: 12,
    backgroundColor: '#f9fafb',
    textAlignVertical: 'center',
  },
  sendButton: {
    width: 40,
    height: 40,
    borderRadius: 20,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#f3f4f6',
  },
  sendButtonDisabled: {
    backgroundColor: '#e5e7eb',
  },
});

export default DirectMessage;