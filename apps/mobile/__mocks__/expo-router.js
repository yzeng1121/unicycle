const mockNavigate = jest.fn();
const mockPush = jest.fn();
const mockReplace = jest.fn();
const mockBack = jest.fn();

module.exports = {
    router: {
        navigate: mockNavigate,
        push: mockPush,
        replace: mockReplace,
        back: mockBack,
    },
    useRouter: () => ({
        navigate: mockNavigate,
        push: mockPush,
        replace: mockReplace,
        back: mockBack,
    }),
    useLocalSearchParams: () => ({}),
    Link: 'Link',
    Redirect: 'Redirect',

    __mockNavigate: mockNavigate,
    __mockPush: mockPush,
};