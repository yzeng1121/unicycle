export const requests = [
  {
    id: '1',
    name: 'Jenna W.',
    initials: 'JW',
    username: 'jennadoodles',
    location: 'Carmichael Hall',
    message: 'Need a HDMI cable tonight for a presentation!',
    time: '10m ago',
    datePosted: '2025-06-05T14:50:00Z',
    bgColor: '#AED6F1',
    category: 'ELECTRONICS'
  },
  {
    id: '2',
    name: 'Kevin L.',
    initials: 'KL',
    username: 'kevinlearns',
    location: 'Carmichael Hall',
    message: 'Looking for a fan to borrow for the weekend',
    time: '25m ago',
    datePosted: '2025-06-05T14:35:00Z',
    bgColor: '#F4D03F',
    category: 'APPLIANCES'
  },
  {
    id: '3',
    name: 'Sarah M.',
    initials: 'SM',
    username: 'sarahstudies',
    location: 'Carmichael Hall',
    message: 'Anyone have a graphing calculator I can borrow for my calc exam tomorrow?',
    time: '1h ago',
    datePosted: '2025-06-05T13:00:00Z',
    bgColor: '#A9DFBF',
    category: 'SCHOOL SUPPLIES'
  },
  {
    id: '4',
    name: 'Mike R.',
    initials: 'MR',
    username: 'mikerides',
    location: 'Carmichael Hall',
    message: 'Need jumper cables ASAP - car won\'t start in the parking lot',
    time: '2h ago',
    datePosted: '2025-06-05T12:00:00Z',
    bgColor: '#F1948A',
    category: 'AUTOMOTIVE'
  },
  {
    id: '5',
    name: 'Lisa C.',
    initials: 'LC',
    username: 'lisacreates',
    location: 'Carmichael Hall',
    message: 'Looking for poster board for tomorrow\'s presentation',
    time: '3h ago',
    datePosted: '2025-06-05T11:00:00Z',
    bgColor: '#D7DBDD',
    category: 'SCHOOL SUPPLIES'
  },
  {
    id: '6',
    name: 'Alex T.',
    initials: 'AT',
    username: 'alextech',
    location: 'Carmichael Hall',
    message: 'Anyone have a phone charger (USB-C) I can borrow for a few hours?',
    time: '4h ago',
    datePosted: '2025-06-05T10:00:00Z',
    bgColor: '#AED6F1',
    category: 'ELECTRONICS'
  },
  {
    id: '7',
    name: 'Emma D.',
    initials: 'ED',
    username: 'emmadance',
    location: 'Carmichael Hall',
    message: 'Need black dress shoes size 7 for formal event tonight!',
    time: '5h ago',
    datePosted: '2025-06-05T09:00:00Z',
    bgColor: '#F4D03F',
    category: 'CLOTHING'
  },
  {
    id: '8',
    name: 'Ryan B.',
    initials: 'RB',
    username: 'ryanbooks',
    location: 'Carmichael Hall',
    message: 'Looking for Campbell Biology textbook to borrow for the weekend',
    time: '6h ago',
    datePosted: '2025-06-05T08:00:00Z',
    bgColor: '#A9DFBF',
    category: 'TEXTBOOKS'
  },
  {
    id: '9',
    name: 'Chloe K.',
    initials: 'CK',
    username: 'chloekitchen',
    location: 'Carmichael Hall',
    message: 'Anyone have a rice cooker I can use for a dorm party this weekend?',
    time: '8h ago',
    datePosted: '2025-06-05T06:00:00Z',
    bgColor: '#F1948A',
    category: 'APPLIANCES'
  },
  {
    id: '10',
    name: 'Josh P.',
    initials: 'JP',
    username: 'joshplays',
    location: 'Carmichael Hall',
    message: 'Need Xbox controller for tournament practice - mine broke',
    time: '12h ago',
    datePosted: '2025-06-05T02:00:00Z',
    bgColor: '#D7DBDD',
    category: 'ELECTRONICS'
  },
  {
    id: '11',
    name: 'Maya S.',
    initials: 'MS',
    username: 'mayasings',
    location: 'Carmichael Hall',
    message: 'Looking for a bluetooth speaker for outdoor study session',
    time: '18h ago',
    datePosted: '2025-06-04T20:00:00Z',
    bgColor: '#AED6F1',
    category: 'ELECTRONICS'
  },
  {
    id: '12',
    name: 'Tyler G.',
    initials: 'TG',
    username: 'tylergym',
    location: 'Carmichael Hall',
    message: 'Anyone have resistance bands I can borrow for PT exercises?',
    time: '1d ago',
    datePosted: '2025-06-04T14:00:00Z',
    bgColor: '#F4D03F',
    category: 'FITNESS'
  },
  {
    id: '13',
    name: 'Zoe L.',
    initials: 'ZL',
    username: 'zoelights',
    location: 'Carmichael Hall',
    message: 'Need a desk lamp with good lighting for late night studying',
    time: '1d ago',
    datePosted: '2025-06-04T12:00:00Z',
    bgColor: '#A9DFBF',
    category: 'FURNITURE'
  },
  {
    id: '14',
    name: 'David H.',
    initials: 'DH',
    username: 'davidhikes',
    location: 'Carmichael Hall',
    message: 'Looking for a backpack for weekend camping trip - mine ripped',
    time: '1d ago',
    datePosted: '2025-06-04T10:00:00Z',
    bgColor: '#F1948A',
    category: 'OUTDOOR'
  },
  {
    id: '15',
    name: 'Grace W.',
    initials: 'GW',
    username: 'gracewrites',
    location: 'Carmichael Hall',
    message: 'Anyone have a good coffee maker I can borrow for finals week?',
    time: '2d ago',
    datePosted: '2025-06-03T14:00:00Z',
    bgColor: '#D7DBDD',
    category: 'APPLIANCES'
  }
];

// Helper function to get time ago (for live updates)
export const getTimeAgo = (datePosted) => {
  const now = new Date();
  const posted = new Date(datePosted);
  const diffMs = now - posted;
  const diffMins = Math.floor(diffMs / (1000 * 60));
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

  if (diffMins < 1) return 'Just now';
  if (diffMins < 60) return `${diffMins}m ago`;
  if (diffHours < 24) return `${diffHours}h ago`;
  if (diffDays < 2) return `${diffDays}d ago`;
  return posted.toLocaleDateString();
};

// Helper functions
export const getUrgentAsks = () => {
  return quickAsks.filter(ask => ask.urgent);
};

export const getAsksByCategory = (category) => {
  return quickAsks.filter(ask => ask.category.toLowerCase() === category.toLowerCase());
};

export const getRecentAsks = (hours = 24) => {
  const cutoff = new Date(Date.now() - (hours * 60 * 60 * 1000));
  return quickAsks.filter(ask => new Date(ask.datePosted) > cutoff);
};