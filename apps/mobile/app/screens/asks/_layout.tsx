import { Stack } from 'expo-router';

export default function RequestLayout() {
  return (
    <Stack>
      <Stack.Screen 
        name="[userId]" 
        options={{ headerShown: false }} 
      />
      <Stack.Screen 
        name="quick-asks" 
        options={{ headerShown: false }} 
      />
    </Stack>
  );
}