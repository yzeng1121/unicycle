import { Stack } from 'expo-router';

export default function ProductLayout() {
  return (
    <Stack>
      <Stack.Screen 
        name="[itemId]" 
        options={{ headerShown: false }} 
      />
      <Stack.Screen 
        name="nearby-items" 
        options={{ headerShown: false }} 
      />
      <Stack.Screen 
        name="suggested-items" 
        options={{ headerShown: false }} 
      />
      <Stack.Screen 
        name="search-items" 
        options={{ headerShown: false }} 
      />
    </Stack>
  );
}