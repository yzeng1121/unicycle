import React, { useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, ScrollView } from 'react-native';

interface DropdownProps {
  label: string;
  placeholder?: string;
  value: string;
  onValueChange: (value: string) => void;
  options: string[];
  required?: boolean;
  maxHeight?: number;
  testID?: string;
}

export const Dropdown: React.FC<DropdownProps> = ({
  label,
  placeholder = 'Select',
  value,
  onValueChange,
  options,
  required = false,
  maxHeight = 200,
}) => {
  const [showDropdown, setShowDropdown] = useState(false);

  const handleSelect = (option: string) => {
    onValueChange(option);
    setShowDropdown(false);
  };

  return (
    <View style={styles.fieldContainer}>
      <Text style={styles.label}>
        {label} {required && '*'}
      </Text>
      <TouchableOpacity 
        style={styles.dropdown}
        onPress={() => setShowDropdown(!showDropdown)}
      >
        <Text style={[styles.dropdownText, !value && styles.placeholderText]}>
          {value || placeholder}
        </Text>
        <Text style={styles.dropdownArrow}>▼</Text>
      </TouchableOpacity>
      {showDropdown && (
        <View style={[styles.dropdownOptions, { maxHeight }]}>
          <ScrollView nestedScrollEnabled>
            {options.map((option, index) => (
              <TouchableOpacity 
                key={index}
                style={styles.dropdownOption}
                onPress={() => handleSelect(option)}
              >
                <Text style={styles.dropdownOptionText}>{option}</Text>
              </TouchableOpacity>
            ))}
          </ScrollView>
        </View>
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  fieldContainer: {
    marginBottom: 20,
    zIndex: 1,
  },
  label: {
    fontSize: 16,
    fontWeight: '600',
    marginBottom: 8,
    color: '#333',
  },
  dropdown: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#DDD',
    borderRadius: 10,
    padding: 15,
    backgroundColor: '#FFF',
  },
  dropdownText: {
    fontSize: 16,
    color: '#000',
    flex: 1,
  },
  placeholderText: {
    color: '#999',
  },
  dropdownArrow: {
    fontSize: 12,
    color: '#666',
    marginLeft: 10,
  },
  dropdownOptions: {
    position: 'absolute',
    top: 70,
    left: 0,
    right: 0,
    backgroundColor: '#FFF',
    borderWidth: 1,
    borderColor: '#DDD',
    borderRadius: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 5,
    zIndex: 1000,
  },
  dropdownOption: {
    padding: 15,
    borderBottomWidth: 1,
    borderBottomColor: '#F0F0F0',
  },
  dropdownOptionText: {
    fontSize: 16,
    color: '#000',
  },
});