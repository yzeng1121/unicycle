import React from 'react';
import { Pressable, PressableProps } from 'react-native';
import * as Haptics from 'expo-haptics';

export type HapticTabProps = PressableProps & {
  children: React.ReactNode;
};

export function HapticTab({ children, onPressIn, ...rest }: HapticTabProps) {
  const handlePressIn = (event: any) => {
    // Trigger haptic feedback on press
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
    
    // Call the original onPressIn if it exists
    onPressIn?.(event);
  };

  return (
    <Pressable onPressIn={handlePressIn} {...rest}>
      {children}
    </Pressable>
  );
}