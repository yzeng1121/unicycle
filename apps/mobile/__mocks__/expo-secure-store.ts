const store: Record<string, string> = {};

export const setItemAsync = jest.fn(async (key: string, value: string) => {
    store[key] = value;
    return Promise.resolve();
});

export const getItemAsync = jest.fn(async (key: string) => {
    return Promise.resolve(store[key] || null);
})

export const deleteItemAsync = jest.fn(async (key: string) => {
    delete store[key];
    return Promise.resolve();
});

export const __clearStore = () => {
    Object.keys(store).forEach(key => delete store[key]);
};

