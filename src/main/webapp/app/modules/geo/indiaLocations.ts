/**
 * indiaLocations.ts
 *
 * Static reference data for Indian states and their major districts.
 * Powers the cascading State → District dropdowns in the search form.
 *
 * This is standard public reference data, not app-specific content.
 */

export interface District {
  name: string;
}

export interface State {
  name: string;
  districts: District[];
}

export const INDIA_STATES: State[] = [
  {
    name: 'Kerala',
    districts: [
      { name: 'Thiruvananthapuram' },
      { name: 'Kollam' },
      { name: 'Pathanamthitta' },
      { name: 'Alappuzha' },
      { name: 'Kottayam' },
      { name: 'Idukki' },
      { name: 'Ernakulam' },
      { name: 'Thrissur' },
      { name: 'Palakkad' },
      { name: 'Malappuram' },
      { name: 'Kozhikode' },
      { name: 'Wayanad' },
      { name: 'Kannur' },
      { name: 'Kasaragod' },
    ],
  },
  {
    name: 'Tamil Nadu',
    districts: [
      { name: 'Chennai' },
      { name: 'Coimbatore' },
      { name: 'Madurai' },
      { name: 'Tiruchirappalli' },
      { name: 'Salem' },
      { name: 'Tirunelveli' },
      { name: 'Vellore' },
      { name: 'Erode' },
      { name: 'Thanjavur' },
      { name: 'Dindigul' },
      { name: 'Krishnagiri' },
      { name: 'Kancheepuram' },
      { name: 'Villupuram' },
      { name: 'Cuddalore' },
      { name: 'Nagapattinam' },
      { name: 'Thoothukudi' },
    ],
  },
  {
    name: 'Karnataka',
    districts: [
      { name: 'Bengaluru Urban' },
      { name: 'Bengaluru Rural' },
      { name: 'Mysuru' },
      { name: 'Mangaluru' },
      { name: 'Hubballi-Dharwad' },
      { name: 'Belagavi' },
      { name: 'Kalaburagi' },
      { name: 'Vijayapura' },
      { name: 'Shivamogga' },
      { name: 'Tumakuru' },
      { name: 'Davanagere' },
      { name: 'Ballari' },
      { name: 'Raichur' },
      { name: 'Hassan' },
      { name: 'Udupi' },
    ],
  },
  {
    name: 'Andhra Pradesh',
    districts: [
      { name: 'Visakhapatnam' },
      { name: 'Vijayawada' },
      { name: 'Guntur' },
      { name: 'Nellore' },
      { name: 'Kurnool' },
      { name: 'Tirupati' },
      { name: 'Kakinada' },
      { name: 'Rajahmundry' },
      { name: 'Anantapur' },
      { name: 'Kadapa' },
      { name: 'Vizianagaram' },
      { name: 'Srikakulam' },
    ],
  },
  {
    name: 'Telangana',
    districts: [
      { name: 'Hyderabad' },
      { name: 'Warangal' },
      { name: 'Nizamabad' },
      { name: 'Karimnagar' },
      { name: 'Khammam' },
      { name: 'Rangareddy' },
      { name: 'Medak' },
      { name: 'Nalgonda' },
    ],
  },
  {
    name: 'Maharashtra',
    districts: [
      { name: 'Mumbai' },
      { name: 'Pune' },
      { name: 'Nagpur' },
      { name: 'Nashik' },
      { name: 'Aurangabad' },
      { name: 'Solapur' },
      { name: 'Kolhapur' },
      { name: 'Amravati' },
      { name: 'Thane' },
      { name: 'Raigad' },
    ],
  },
  {
    name: 'Gujarat',
    districts: [
      { name: 'Ahmedabad' },
      { name: 'Surat' },
      { name: 'Vadodara' },
      { name: 'Rajkot' },
      { name: 'Bhavnagar' },
      { name: 'Jamnagar' },
      { name: 'Gandhinagar' },
      { name: 'Anand' },
    ],
  },
  {
    name: 'Rajasthan',
    districts: [
      { name: 'Jaipur' },
      { name: 'Jodhpur' },
      { name: 'Udaipur' },
      { name: 'Kota' },
      { name: 'Bikaner' },
      { name: 'Ajmer' },
      { name: 'Bharatpur' },
      { name: 'Alwar' },
    ],
  },
  {
    name: 'Uttar Pradesh',
    districts: [
      { name: 'Lucknow' },
      { name: 'Kanpur' },
      { name: 'Agra' },
      { name: 'Varanasi' },
      { name: 'Meerut' },
      { name: 'Allahabad (Prayagraj)' },
      { name: 'Ghaziabad' },
      { name: 'Noida (Gautam Buddha Nagar)' },
      { name: 'Mathura' },
      { name: 'Bareilly' },
    ],
  },
  {
    name: 'Delhi',
    districts: [
      { name: 'Central Delhi' },
      { name: 'East Delhi' },
      { name: 'New Delhi' },
      { name: 'North Delhi' },
      { name: 'North East Delhi' },
      { name: 'North West Delhi' },
      { name: 'Shahdara' },
      { name: 'South Delhi' },
      { name: 'South East Delhi' },
      { name: 'South West Delhi' },
      { name: 'West Delhi' },
    ],
  },
  {
    name: 'West Bengal',
    districts: [
      { name: 'Kolkata' },
      { name: 'Howrah' },
      { name: 'North 24 Parganas' },
      { name: 'South 24 Parganas' },
      { name: 'Hooghly' },
      { name: 'Burdwan' },
      { name: 'Nadia' },
      { name: 'Murshidabad' },
    ],
  },
  {
    name: 'Punjab',
    districts: [
      { name: 'Amritsar' },
      { name: 'Ludhiana' },
      { name: 'Jalandhar' },
      { name: 'Patiala' },
      { name: 'Bathinda' },
      { name: 'Mohali' },
      { name: 'Gurdaspur' },
    ],
  },
  {
    name: 'Haryana',
    districts: [
      { name: 'Gurugram' },
      { name: 'Faridabad' },
      { name: 'Hisar' },
      { name: 'Ambala' },
      { name: 'Rohtak' },
      { name: 'Panipat' },
      { name: 'Sonipat' },
    ],
  },
  {
    name: 'Madhya Pradesh',
    districts: [
      { name: 'Bhopal' },
      { name: 'Indore' },
      { name: 'Jabalpur' },
      { name: 'Gwalior' },
      { name: 'Ujjain' },
      { name: 'Rewa' },
      { name: 'Sagar' },
    ],
  },
  {
    name: 'Bihar',
    districts: [
      { name: 'Patna' },
      { name: 'Gaya' },
      { name: 'Bhagalpur' },
      { name: 'Muzaffarpur' },
      { name: 'Darbhanga' },
      { name: 'Purnia' },
    ],
  },
  {
    name: 'Odisha',
    districts: [
      { name: 'Bhubaneswar' },
      { name: 'Cuttack' },
      { name: 'Rourkela' },
      { name: 'Berhampur' },
      { name: 'Sambalpur' },
      { name: 'Balasore' },
    ],
  },
];

/** Get districts for a given state name */
export const getDistricts = (stateName: string): District[] => {
  const state = INDIA_STATES.find(s => s.name === stateName);
  return state ? state.districts : [];
};
