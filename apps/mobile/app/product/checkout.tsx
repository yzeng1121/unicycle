import { useEffect, useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ActivityIndicator,
  Alert,
} from 'react-native';
import { useLocalSearchParams, router } from 'expo-router';
import { useStripe } from '@stripe/stripe-react-native';
import { Ionicons } from '@expo/vector-icons';

import { useAuth } from '../../contexts/AuthContext';

export default function CheckoutScreen() {
  const { itemId } = useLocalSearchParams<{ itemId: string }>();
  const { initPaymentSheet, presentPaymentSheet } = useStripe();
  const { makeAuthenticatedRequest } = useAuth();

  const [loading, setLoading] = useState(true);
  const [paymentReady, setPaymentReady] = useState(false);

  useEffect(() => {
    initializePayment();
  }, [itemId]);

  const initializePayment = async () => {
    try {
      const response = await makeAuthenticatedRequest(
        'http://13.221.95.208:8080/product/checkout',
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ listingId: itemId, currency: 'usd' }),
        }
      );

      const data = await response.json();

      if (!response.ok || data.status !== 'SUCCESS') {
        Alert.alert('Error', data.message || 'Could not start checkout.');
        router.back();
        return;
      }

      const { error } = await initPaymentSheet({
        paymentIntentClientSecret: data.clientSecret,
        merchantDisplayName: 'UniCycle',
      });

      if (error) {
        Alert.alert('Error', error.message);
        router.back();
        return;
      }

      setPaymentReady(true);
    } catch (err) {
      Alert.alert('Error', 'Failed to initialize payment.');
      router.back();
    } finally {
      setLoading(false);
    }
  };

  const handlePay = async () => {
    const { error } = await presentPaymentSheet();

    if (error) {
      if (error.code !== 'Canceled') {
        Alert.alert('Payment failed', error.message);
      }
      return;
    }

    Alert.alert(
      'Payment successful!',
      'Your purchase is confirmed. Arrange pickup with the seller.',
      [{ text: 'OK', onPress: () => router.replace('/(tabs)') }]
    );
  };

  if (loading) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" color="#000" />
        <Text style={styles.loadingText}>Preparing checkout…</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <TouchableOpacity onPress={() => router.back()} style={styles.backButton}>
          <Ionicons name="chevron-back" size={24} color="#000" />
        </TouchableOpacity>
        <Text style={styles.title}>Checkout</Text>
      </View>

      <View style={styles.body}>
        <Text style={styles.infoText}>
          Tap the button below to securely enter your card details via Stripe.
        </Text>

        <TouchableOpacity
          style={[styles.payButton, !paymentReady && styles.payButtonDisabled]}
          onPress={handlePay}
          disabled={!paymentReady}
        >
          <Text style={styles.payButtonText}>Pay with Card</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  centered: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#fff',
  },
  loadingText: {
    marginTop: 12,
    fontSize: 14,
    color: '#666',
  },
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingTop: 50,
    paddingBottom: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#000',
  },
  backButton: {
    padding: 5,
    marginRight: 12,
  },
  title: {
    fontSize: 18,
    fontWeight: 'bold',
  },
  body: {
    flex: 1,
    padding: 24,
    justifyContent: 'center',
  },
  infoText: {
    fontSize: 15,
    color: '#444',
    textAlign: 'center',
    marginBottom: 32,
    lineHeight: 22,
  },
  payButton: {
    backgroundColor: '#000',
    borderRadius: 8,
    paddingVertical: 16,
    alignItems: 'center',
  },
  payButtonDisabled: {
    backgroundColor: '#ccc',
  },
  payButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: 'bold',
  },
});
