import { imageMap } from '../../assets/images/ImageMapper'; // Import the imageMap

export const data = [
  {
    id: '1',
    itemName: "Arc'teryx Women's Cream and Grey Jacket",
    listingType: 'Selling',
    category: 'CLOTHING',
    subcategory: 'TOPS',
    size: 'XXS',
    condition: 'Good',
    price: 30.00,
    images: [
      '/assets/images/productImages/arcteryx-womens-cream-and-grey-jacket-1.jpg',
      '/assets/images/productImages/arcteryx-womens-cream-and-grey-jacket-2.jpg',
      '/assets/images/productImages/arcteryx-womens-cream-and-grey-jacket-3.jpg',
      '/assets/images/productImages/arcteryx-womens-cream-and-grey-jacket-4.jpg',
      '/assets/images/productImages/arcteryx-womens-cream-and-grey-jacket-5.jpg',
    ],
    seller: {
      id: 'user1',
      username: 'pambeesly',
      profilePhoto: '/assets/images/userImages/Pam_Beesley.jpg',
      rating: 4.8
    },
    description: 'Super cute Patagonia fleece jacket. Kids L fits like XXS/XS. Sleeves too short for me now. Message me with questions!',
    datePosted: '2024-05-28',
    location: 'Off-Campus',
  },
  {
    id: '2',
    itemName: 'Silver Nikon COOLPIX S6000 14.2MP Digital Camera Lens Cover Defective Untested',
    listingType: 'Selling',
    category: 'ELECTRONICS',
    subcategory: 'CAMERAS',
    condition: 'Excellent',
    price: 120.00,
    images: [
      '/assets/images/productImages/Silver-Nikon-COOLPIX-S6000-1.jpg',
      '/assets/images/productImages/Silver-Nikon-COOLPIX-S6000-2.jpg',
      '/assets/images/productImages/Silver-Nikon-COOLPIX-S6000-3.jpg',
      '/assets/images/productImages/Silver-Nikon-COOLPIX-S6000-4.jpg',
      '/assets/images/productImages/Silver-Nikon-COOLPIX-S6000-5.jpg',
      '/assets/images/productImages/Silver-Nikon-COOLPIX-S6000-6.jpg'
    ],
    seller: {
      id: 'user2',
      username: 'andybernard',
      profilePhoto: '/assets/images/userImages/Andy_Bernard.jpg',
      rating: 2.9
    },
    description: 'Capture precious moments with this silver Nikon COOLPIX S6000 digital camera. With a maximum resolution of 14.2 MP, this compact camera features a 7x optical zoom and 2x digital zoom for clear shots. The 2.25" screen size allows you to view your pictures and videos with ease. This unit comes with features such as blink detection, face detection, audio recording, and image stabilization to ensure you take the perfect shot every time. The camera is powered by a lithium-ion battery and has a weight of 5.44 oz. It also has a USB connectivity option for easy transfer of your files. Buy this Nikon COOLPIX S6000 today and start capturing memories! This camera is untested. No battery or SD card included.',
    datePosted: '2024-05-27',
    location: 'Carmichael Hall',
  },
  {
    id: '3',
    itemName: 'Large Maroon Couch',
    listingType: 'Giveaway',
    category: 'FURNITURE',
    subcategory: 'CHAIRS',
    condition: 'Used',
    images: [
      '/assets/images/productImages/sofa.jpg'
    ],
    seller: {
      id: 'user3',
      username: 'michaelscott',
      profilePhoto: '/assets/images/userImages/Michael_Scott.png',
      rating: 1.4
    },
    description: "I'm moving out and need to get rid of this couch, it's right outside of Hodge.",
    datePosted: '2022-06-22',
    location: 'Hodgedon Hall',
  },
  {
    id: '4',
    itemName: 'Campbell Biology (Campbell Biology Series)',
    listingType: 'Trading',
    category: 'TEXTBOOKS',
    subcategory: 'BIOLOGY',
    condition: 'Excellent',
    images: [
      '/assets/images/productImages/Campbell-Biology-Series-1.jpg',
      '/assets/images/productImages/Campbell-Biology-Series-2.jpg',
      '/assets/images/productImages/Campbell-Biology-Series-3.jpg',
      '/assets/images/productImages/Campbell-Biology-Series-4.jpg',
      '/assets/images/productImages/Campbell-Biology-Series-5.jpg',
      '/assets/images/productImages/Campbell-Biology-Series-6.jpg'
    ],
    seller: {
      id: 'user4',
      username: 'dwightschrute',
      profilePhoto: '/assets/images/userImages/Dwight_Schrute.jpg',
      rating: 4.9
    },
    description: 'Minor ware and tare, as you can see on the pictures. Looking for a Chemistry textbook!',
    datePosted: '2025-05-25',
    location: 'Hill Hall',
  },
  {
    id: '5',
    itemName: 'Dirt Devil Endura Reach Upright Vacuum Cleaner - Red (UD20124)',
    listingType: 'Lending',
    category: 'APPLIANCES',
    subcategory: 'CLEANING',
    condition: 'Excellent',
    price: 5.00,
    rate: 'per day',
    images: [
      '/assets/images/productImages/Dirt-Devil-Endura-Reach-Upright-Vacuum-Cleaner-1.webp',
      '/assets/images/productImages/Dirt-Devil-Endura-Reach-Upright-Vacuum-Cleaner-2.webp',
      '/assets/images/productImages/Dirt-Devil-Endura-Reach-Upright-Vacuum-Cleaner-3.webp'
    ],
    seller: {
      id: 'user5',
      username: 'jimhalpert',
      profilePhoto: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=50&h=50&fit=crop&crop=face',
      rating: 4.3
    },
    description: "Come pick up any time. I'm on Houston 2nd floor.",
    datePosted: '2024-05-24',
    location: 'Houston Hall',
  },
  {
    id: '6',
    itemName: 'Red Reclining Gaming Chair',
    listingType: 'Selling',
    category: 'FURNITURE',
    subcategory: 'CHAIRS',
    condition: 'Good',
    price: 60.00,
    images: [
      '/assets/images/productImages/red-reclining-gaming-chair-1.webp'
    ],
    seller: {
      id: 'user6',
      username: 'ryanhoward',
      profilePhoto: '/assets/images/userImages/Ryan_Howard.jpg',
      rating: 5.0
    },
    description: "gaming chair. The only flaw is it does squeak pretty easily but it's not too loud, it reclines",
    datePosted: '2024-05-23',
    location: 'Latin Way Apartments',
  },
];

// Helper functions for working with the data
export const getProductById = (id) => {
  return sampleProducts.find(product => product.id === id);
};

export const getProductsByCategory = (category) => {
  return sampleProducts.filter(product => 
    product.category.toLowerCase() === category.toLowerCase()
  );
};

export const getProductsByCondition = (condition) => {
  return sampleProducts.filter(product => 
    product.condition.toLowerCase() === condition.toLowerCase()
  );
};

export const getProductsBySeller = (sellerId) => {
  return sampleProducts.filter(product => product.seller.id === sellerId);
};

export const searchProducts = (query) => {
  const lowercaseQuery = query.toLowerCase();
  return sampleProducts.filter(product => 
    product.itemName.toLowerCase().includes(lowercaseQuery) ||
    product.description.toLowerCase().includes(lowercaseQuery) ||
    product.tags.some(tag => tag.toLowerCase().includes(lowercaseQuery))
  );
};

// Gets the first image of an item
export const getItemImage = (item, imageIndex) => {
  const imagePath = item.images[imageIndex];
  return imageMap[imagePath] || require('../../assets/images/placeholder.jpg');
};

// Sample usage examples:
// import { sampleProducts, getProductById, searchProducts } from './sampleProducts';
// 
// // Get all products
// console.log(sampleProducts);
// 
// // Get specific product
// const product = getProductById('1');
// 
// // Search products
// const results = searchProducts('vintage');